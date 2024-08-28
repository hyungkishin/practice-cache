package com.example.cachepractice.common.aop;

import com.example.cachepractice.domain.MockData;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private static final String REDIS_DATA_KEY_PREFIX = "data:";
    private static final String DELIMITER = ",";

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("execution(* com.example.cachepractice.ui.DataController.getItems(..)) && args(idString)")
    public Object sortIdsBeforeProcessing(ProceedingJoinPoint joinPoint, String idString) throws Throwable {
        String sortedIds = Arrays.stream(idString.split(DELIMITER))
                .map(String::trim)
                .map(Long::parseLong)
                .sorted()
                .map(Object::toString)
                .collect(Collectors.joining(DELIMITER));
;
        return joinPoint.proceed(new Object[]{sortedIds});
    }

    @Around("execution(* com.example.cachepractice.application.DataService.getDataByIds(..)) && args(sortedIds)")
    public Object checkRedisBeforeService(ProceedingJoinPoint joinPoint, String sortedIds) throws Throwable {
        // ID 리스트 파싱
        List<Long> idList = Arrays.stream(sortedIds.split(DELIMITER))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Redis에서 데이터 조회
        Map<Long, MockData> cachedDataMap = getCachedDataFromRedis(idList);
        List<MockData> cachedData = new ArrayList<>(cachedDataMap.values());

        // 캐시되지 않은 ID 추출
        List<Long> missingIds = idList.stream()
                .filter(id -> !cachedDataMap.containsKey(id))
                .toList();

        // 캐시되지 않은 ID가 있으면 DB 조회
        if (!missingIds.isEmpty()) {
            String missingIdsStr = missingIds.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(DELIMITER));
            List<MockData> dbData = (List<MockData>) joinPoint.proceed(new Object[]{missingIdsStr});

            // 조회된 데이터를 Redis에 캐싱
            cacheDataToRedis(dbData);

            // 결과 병합
            cachedData.addAll(dbData);
        }

        return cachedData;
    }

    private Map<Long, MockData> getCachedDataFromRedis(List<Long> ids) {
        return ids.stream()
                .map(id -> redisTemplate.opsForValue().get(REDIS_DATA_KEY_PREFIX + id))
                .filter(Objects::nonNull)
                .map(data -> (MockData) data)
                .collect(Collectors.toMap(MockData::getId, data -> data));
    }

    private void cacheDataToRedis(List<MockData> data) {
        data.forEach(mockData -> {
            String key = REDIS_DATA_KEY_PREFIX + mockData.getId();
            redisTemplate.opsForValue().set(key, mockData);
        });
    }

}


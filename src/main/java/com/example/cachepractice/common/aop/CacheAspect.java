package com.example.cachepractice.common.aop;

import com.example.cachepractice.domain.MockData;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Aspect
@RequiredArgsConstructor
public class CacheAspect<T> {

    private static final String REDIS_DATA_KEY_PREFIX = "data:";
    private static final String DELIMITER = ",";

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(com.example.cachepractice.annotation.Cached) && args(idString)")
    public Object checkRedisBeforeService(ProceedingJoinPoint joinPoint, String idString) throws Throwable {
        // ID 문자열 정렬
        String sortedIds = Arrays.stream(idString.split(DELIMITER))
                .map(String::trim)
                .map(Long::parseLong)
                .sorted()
                .map(Object::toString)
                .collect(Collectors.joining(DELIMITER));

        // ID 리스트 파싱
        List<Long> idList = Arrays.stream(sortedIds.split(DELIMITER))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Redis에서 데이터 조회
        Map<Long, T> cachedDataMap = getCachedDataFromRedis(idList);
        List<T> cachedData = new ArrayList<>(cachedDataMap.values());

        // 캐시되지 않은 ID 추출
        List<Long> missingIds = idList.stream()
                .filter(id -> !cachedDataMap.containsKey(id))
                .collect(Collectors.toList());

        // 캐시되지 않은 ID가 있으면 DB 조회
        if (!missingIds.isEmpty()) {
            String missingIdsStr = joinIds(missingIds);
            List<T> dbData = (List<T>) joinPoint.proceed(new Object[]{missingIdsStr});

            // 조회된 데이터를 Redis에 캐싱
            cacheDataToRedis(dbData);

            // 결과 병합
            cachedData.addAll(dbData);
        }

        return cachedData;
    }

    private Map<Long, T> getCachedDataFromRedis(List<Long> ids) {
        return ids.stream()
                .map(id -> redisTemplate.opsForValue().get(REDIS_DATA_KEY_PREFIX + id))
                .filter(Objects::nonNull)
                .map(data -> (T) data)
                .collect(Collectors.toMap(this::getIdFromObject, data -> data));
    }

    private void cacheDataToRedis(List<T> data) {
        data.forEach(item -> {
            String key = REDIS_DATA_KEY_PREFIX + getIdFromObject(item);
            redisTemplate.opsForValue().set(key, item);
        });
    }

    private String joinIds(List<Long> ids) {
        return ids.stream()
                .map(Object::toString)
                .collect(Collectors.joining(DELIMITER));
    }

    private Long getIdFromObject(T item) {
        try {
            Field field = item.getClass().getDeclaredField("id");
            field.setAccessible(true);
            return (Long) field.get(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Object 에 id 읎다잉", e);
        }
    }

}


package com.example.cachepractice.application;

import com.example.cachepractice.domain.MockData;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataService {

    private final MockDataRepository mockDataRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_DATA_KEY_PREFIX = "data:";

    private static final String SPRING_CACHE_DATA_KEY = "data";

    private static final String DELIMITER = ",";

    @Cacheable(value = SPRING_CACHE_DATA_KEY, key = "#ids")
    public List<MockData> getDataByIds(String ids) {
        List<Long> idList = parseIds(ids);
        List<MockData> cachedData = getCachedData(idList);

        if (isMissingData(idList, cachedData)) {
            List<Long> missingIds = getMissingIds(idList, cachedData);
            List<MockData> dbData = fetchFromDbAndCache(missingIds);
            cachedData.addAll(dbData);
        }
        return cachedData;
    }

    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(DELIMITER))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private List<MockData> getCachedData(List<Long> ids) {
        return ids.stream()
                .map(this::fetchIndividualCachedData)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isMissingData(List<Long> idList, List<MockData> cachedData) {
        return cachedData.size() != idList.size();
    }

    private List<Long> getMissingIds(List<Long> idList, List<MockData> cachedData) {
        return idList.stream()
                .filter(id -> cachedData.stream().noneMatch(data -> data.getId().equals(id)))
                .collect(Collectors.toList());
    }

    // 해당하는 ID 를 Redis 캐시에서 조회한다.
    @Cacheable(value = "individual", key = "#id")
    public MockData fetchIndividualCachedData(Long id) {
        return (MockData) redisTemplate.opsForValue().get(REDIS_DATA_KEY_PREFIX + id);
    }

    /**
     * ID 리스트에 해당하는 데이터를 조회하고, 조회된 각 데이터를 Redis 캐시에 저장한다.
     * @param ids: 캐싱되지 않은 ids
     * @return mockData
     */
    private List<MockData> fetchFromDbAndCache(List<Long> ids) {
        return ids.stream()
                .map(this::getDetailDataAndCache)
                .collect(Collectors.toList());
    }

    private MockData getDetailDataAndCache(Long id) {
        MockData data = mockDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID: " + id));

        redisTemplate.opsForValue().set(REDIS_DATA_KEY_PREFIX + id, data);
        return data;
    }

}

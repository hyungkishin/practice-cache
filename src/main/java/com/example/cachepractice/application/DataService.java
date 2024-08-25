package com.example.cachepractice.application;

import com.example.cachepractice.domain.MockData;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataService {

    private final MockDataRepository mockDataRepository;

    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    public List<MockData> getDataByIds(List<Long> ids) {
        Cache cache = cacheManager.getCache("data");

        List<MockData> cachedDatas = ids.stream()
                .map(id -> Objects.requireNonNull(cache).get(id, MockData.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Long> cachedIds = cachedDatas.stream()
                .map(MockData::getId)
                .toList();

        List<Long> missingIds = ids.stream()
                .filter(id -> !cachedIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            addCache(missingIds, cache, cachedDatas);
        }

        return cachedDatas;
    }

    private void addCache(List<Long> missingIds, Cache cache, List<MockData> cachedDatas) {
        missingIds.forEach(it -> {
            MockData dataById = getDataById(it);
            cache.put(dataById.getId(), dataById);
            cachedDatas.add(dataById);
        });
    }

    public MockData getDataById(Long id) {
        return mockDataRepository.findById(id)
                .orElse(null);
    }

}

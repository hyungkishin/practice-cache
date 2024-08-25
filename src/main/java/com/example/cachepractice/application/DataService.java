package com.example.cachepractice.application;

import com.example.cachepractice.domain.MockData;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataService {

    private final MockDataRepository mockDataRepository;

    @Cacheable(value = "data", key = "#id")
    public MockData getDataById(Long id) {
        // 데이터 조회 로직 (예: DB에서 데이터 조회)
        return fetchDataFromDatabase(id);
    }

    private MockData fetchDataFromDatabase(Long id) {
        // 실제 데이터베이스 조회 로직
        return mockDataRepository.findById(id).orElse(null);
    }

    public List<MockData> getDataByIds(List<Long> ids) {
        // Redis에서 캐시된 데이터 가져오기
        List<MockData> cachedData = ids.stream()
                .map(this::getDataById)
                .filter(data -> data != null)
                .collect(Collectors.toList());

        // 캐시되지 않은 ID 찾기
        List<Long> cachedIds = cachedData.stream()
                .map(MockData::getId)
                .collect(Collectors.toList());
        List<Long> missingIds = ids.stream()
                .filter(id -> !cachedIds.contains(id))
                .collect(Collectors.toList());

        // DB에서 조회하여 캐싱
        List<MockData> dbData = fetchDataFromDatabase(missingIds);
        dbData.forEach(data -> cacheDataById(data.getId(), data));

        // 모든 데이터를 합쳐서 반환
        cachedData.addAll(dbData);
        return cachedData;
    }

    private List<MockData> fetchDataFromDatabase(List<Long> ids) {
        // 실제 데이터베이스 조회 로직
        return mockDataRepository.findAllById(ids);
    }

    @CachePut(value = "data", key = "#data.id")
    public MockData cacheDataById(Long id, MockData data) {
        return data;
    }

}

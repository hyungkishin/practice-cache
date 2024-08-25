package com.example.cachepractice.application;

import com.example.cachepractice.domain.MockData;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DataService {

    private final MockDataRepository mockDataRepository;

    public MockData getDataById(Long id) {
        return mockDataRepository.findById(id).orElse(null);
    }

    public List<MockData> getDataByIds(List<Long> ids) {
        return mockDataRepository.findAllById(ids);
    }

    @CachePut(value = "data", key = "#data.id")
    public MockData cacheDataById(Long id, MockData data) {
        return data;
    }

}

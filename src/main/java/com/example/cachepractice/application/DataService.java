package com.example.cachepractice.application;

import com.example.cachepractice.domain.MockData;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataService {

    private final com.example.cachepractice.application.MockDataRepository mockDataRepository;

    @Cacheable(value = "dataCache", key = "#ids")
    public List<MockData> getDataByIds(String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return mockDataRepository.findAllById(idList);
    }

}

package com.example.cachepractice.application;

import com.example.cachepractice.domain.MockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataServiceTest {

    @Mock
    private MockDataRepository mockDataRepository;

    @InjectMocks
    private DataService dataService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetDataByIds() {
        String ids = "1,2,3";
        MockData mockData1 = createMockData(1L, "data1", 31);
        MockData mockData2 = createMockData(2L, "data2", 31);
        MockData mockData3 = createMockData(3L, "data3", 31);

        when(mockDataRepository.findAllById(anyList())).thenReturn(Arrays.asList(mockData1, mockData2, mockData3));

        List<MockData> result = dataService.getDataByIds(ids);
        assertEquals(3, result.size());
    }

    private MockData createMockData(Long id, String name, int age) {
        return new MockData(id, name, age);
    }

}
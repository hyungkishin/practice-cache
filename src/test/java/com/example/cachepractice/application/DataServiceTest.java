package com.example.cachepractice.application;

import com.example.cachepractice.domain.MockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private DataService dataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGetDataByIds_AllCached() {
        // Given
        String ids = "1,2,3";
        MockData data1 = createMockData(1L, "Alice", 30);
        MockData data2 = createMockData(2L, "Bob", 25);
        MockData data3 = createMockData(3L, "Charlie", 35);

        when(valueOperations.get("data:1")).thenReturn(data1);
        when(valueOperations.get("data:2")).thenReturn(data2);
        when(valueOperations.get("data:3")).thenReturn(data3);

        // When
        List<MockData> results = dataService.getDataByIds(ids);

        // Then
        assertEquals(3, results.size());
        verify(mockDataRepository, never())
                .findById(anyLong());
    }



    @Test
    void testGetDataByIds_SomeMissing() {
        // Given
        String ids = "1,2,4";
        MockData data1 = createMockData(1L, "Alice", 30);
        MockData data2 = createMockData(2L, "Bob", 25);
        MockData missingData = createMockData(4L, "David", 40);

        when(valueOperations.get("data:1")).thenReturn(data1);
        when(valueOperations.get("data:2")).thenReturn(data2);
        when(valueOperations.get("data:4")).thenReturn(null);
        when(mockDataRepository.findById(4L)).thenReturn(Optional.of(missingData));

        // When
        List<MockData> results = dataService.getDataByIds(ids);

        // Then
        assertEquals(3, results.size());
        verify(mockDataRepository, times(1)).findById(4L);
        verify(valueOperations, times(1)).set("data:4", missingData);
    }

    @Test
    void testGetDataByIds_NoneCached() {
        // Given
        String ids = "5,6,7";
        MockData data5 = createMockData(5L, "Eve", 22);
        MockData data6 = createMockData(6L, "Frank", 29);
        MockData data7 = createMockData(7L, "Grace", 31);

        when(valueOperations.get(anyString())).thenReturn(null);
        when(mockDataRepository.findById(5L)).thenReturn(Optional.of(data5));
        when(mockDataRepository.findById(6L)).thenReturn(Optional.of(data6));
        when(mockDataRepository.findById(7L)).thenReturn(Optional.of(data7));

        // When
        List<MockData> results = dataService.getDataByIds(ids);

        // Then
        assertEquals(3, results.size());
        verify(mockDataRepository, times(3)).findById(anyLong());
        verify(valueOperations, times(3)).set(anyString(), any());
    }

    private MockData createMockData(Long id, String name, int age) {
        return new MockData(id, name, age);
    }

}
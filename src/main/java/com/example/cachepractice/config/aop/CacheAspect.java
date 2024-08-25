package com.example.cachepractice.config.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private static final String DELIMITER = ",";

    private final CacheManager cacheManager;

    @Around("execution(* com.example.cachepractice.ui.DataController.getItems(..)) && args(idString)")
    public Object sortAndRemoveDuplicates(ProceedingJoinPoint joinPoint, String idString) throws Throwable {
        List<Long> sortedIds = Arrays.stream(idString.split(DELIMITER))
                .map(Long::valueOf)
                .distinct()
                .sorted()
                .toList();

        // 정렬된 리스트를 새 인자로 전달
        String sortedIdString = sortedIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(DELIMITER));

        return joinPoint.proceed(new Object[]{sortedIdString});
    }

}


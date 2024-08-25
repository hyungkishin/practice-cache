package com.example.cachepractice.config.aop;

import com.example.cachepractice.domain.MockData;
import com.example.cachepractice.ui.request.IdRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    @Around("execution(* com.example.cachepractice.application.DataService.getDataByIds(..)) && args(ids)")
    public Object cacheableAdvice(ProceedingJoinPoint joinPoint, List<Long> ids) throws Throwable {
        List<MockData> cachedData = ids.stream()
                .map(id -> Objects.requireNonNull(cacheManager.getCache("data")).get(id, MockData.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Long> cachedIds = cachedData.stream()
                .map(MockData::getId)
                .toList();

        List<Long> missingIds = ids.stream()
                .filter(id -> !cachedIds.contains(id))
                .collect(Collectors.toList());

        if (!missingIds.isEmpty()) {
            List<MockData> dbData = (List<MockData>) joinPoint.proceed(new Object[]{missingIds});

            dbData.forEach(data -> cacheManager.getCache("data").put(data.getId(), data));
            cachedData.addAll(dbData);
        }

        return cachedData;
    }

    @AfterReturning(pointcut = "execution(* com.example.cachepractice.application.DataService.getDataById(..)) && args(id)", returning = "result", argNames = "joinPoint,id,result")
    public void cacheAfterReturning(JoinPoint joinPoint, Long id, MockData result) {
        if (result != null) {
            cacheManager.getCache("data").put(id, result);
        }
    }


    @AfterThrowing(pointcut = "execution(* com.example.cachepractice.application.DataService.getDataById(..)) && args(id)", throwing = "ex")
    public void cacheAfterThrowing(JoinPoint joinPoint, Long id, Throwable ex) {
        cacheManager.getCache("data").evict(id);
    }
}


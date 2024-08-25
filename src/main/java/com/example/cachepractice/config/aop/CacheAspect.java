package com.example.cachepractice.config.aop;

import com.example.cachepractice.domain.MockData;
import com.example.cachepractice.ui.request.IdRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// @Aspect 어노테이션을 붙여 이 클래스가 Aspect를 나타내는 클래스라는 것을 명시하고 @Component를 붙여 스프링 빈으로 등록
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private final CacheManager cacheManager;

    @Before("execution(* com.example.cachepractice.ui.DataController.getItems(..)) && args(idRequest)")
    public void sortAndRemoveDuplicates(IdRequest idRequest) {
        List<Long> distinctSortedIds = idRequest.getIds().stream()
                .distinct() // 중복 제거
                .sorted()   // 정렬
                .collect(Collectors.toList());

        idRequest.getIds().clear();
        idRequest.getIds().addAll(distinctSortedIds);
    }

    @Around("execution(* com.example.cachepractice.application.DataService.getDataByIds(..)) && args(ids)")
    public Object cacheableAdvice(ProceedingJoinPoint joinPoint, List<Long> ids) throws Throwable {
        // 캐시 키 생성 로직
        List<MockData> cachedData = ids.stream()
                .map(id -> cacheManager.getCache("data").get(id, MockData.class))
                .filter(data -> data != null)
                .collect(Collectors.toList());

        // 캐시되지 않은 ID 찾기
        List<Long> cachedIds = cachedData.stream()
                .map(MockData::getId)
                .collect(Collectors.toList());

        List<Long> missingIds = ids.stream()
                .filter(id -> !cachedIds.contains(id))
                .collect(Collectors.toList());

        if (!missingIds.isEmpty()) {
            // 캐시에 데이터가 없으면 원본 메소드 실행
            List<MockData> dbData = (List<MockData>) joinPoint.proceed(new Object[]{missingIds});

            // 결과를 캐시에 저장
            dbData.forEach(data -> cacheManager.getCache("data").put(data.getId(), data));

            cachedData.addAll(dbData);
        }

        return cachedData;
    }

}

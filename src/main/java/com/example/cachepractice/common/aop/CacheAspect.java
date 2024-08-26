package com.example.cachepractice.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class CacheAspect {

    private static final String DELIMITER = ",";

    @Pointcut("execution(* com.example.cachepractice.ui.DataController.getItems(..)) && args(idString)")
    public void sortPointcut(String idString) {
    }

    @Around(value = "sortPointcut(idString)", argNames = "joinPoint,idString")
    public Object sortIds(ProceedingJoinPoint joinPoint, String idString) throws Throwable {
        String sortedIds = Arrays.stream(idString.split(DELIMITER))
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining(DELIMITER));
        Object[] args = new Object[] {sortedIds};
        return joinPoint.proceed(args);
    }

}


package com.example.cachepractice.common.config;

import com.example.cachepractice.common.aop.CacheAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheAspect cacheAspect(RedisTemplate<String, Object> redisTemplate) {
        return new CacheAspect(redisTemplate);
    }

}
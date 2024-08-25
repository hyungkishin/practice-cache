package com.example.cachepractice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CachePracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CachePracticeApplication.class, args);
    }

}

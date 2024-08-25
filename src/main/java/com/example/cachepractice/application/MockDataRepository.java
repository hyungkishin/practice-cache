package com.example.cachepractice.application;

import com.example.cachepractice.domain.MockData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MockDataRepository extends JpaRepository<MockData, Long> {

}

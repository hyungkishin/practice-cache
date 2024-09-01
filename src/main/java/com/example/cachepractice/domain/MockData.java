package com.example.cachepractice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class MockData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "people_name")
    private String peopleName;

    private Integer age;

    public MockData() {
    }

    // ME : TEST 코드를 위해 임시로 작성한 생성자
    public MockData(Long id, String peopleName, Integer age) {
        this.id = id;
        this.peopleName = peopleName;
        this.age = age;
    }

}

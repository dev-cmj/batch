package com.project.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 데이터베이스의 person 테이블과 매핑되는 데이터 모델 클래스입니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private Long id;
    private String lastName;
    private String firstName;
}

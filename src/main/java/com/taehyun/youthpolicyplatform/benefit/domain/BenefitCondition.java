package com.taehyun.youthpolicyplatform.benefit.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 정책 신청 조건을 저장하는 엔티티
@Getter
@NoArgsConstructor
@Entity
public class BenefitCondition {

    // 조건 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 비교 대상 필드명
    private String fieldName;

    // 비교 연산자
    private String operator;

    // 비교 기준값
    private String value;

    // 필수 조건 여부
    private Boolean required;

    // 이 조건이 속한 정책
    @ManyToOne
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;
}
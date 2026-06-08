package com.taehyun.youthpolicyplatform.user.domain;

import jakarta.persistence.*;

// 정책 판별에 공통적으로 사용되는 사용자 기본 조건 정보
@Entity
public class UserProfile {

    // 프로필 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 나이
    private Integer age;

    // 거주 지역
    private String region;

    // 가구원 수
    private Integer householdSize;

    // 월소득
    private Integer monthlyIncome;

    // 연소득
    private Integer annualIncome;

    // 중위소득 비율
    private Integer middleIncomePercent;

    // 취업 여부
    private Boolean employed;

    // 학생 여부
    private Boolean student;

    // 무주택 여부
    private Boolean houseOwner;

    // 해당 프로필을 작성한 회원
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
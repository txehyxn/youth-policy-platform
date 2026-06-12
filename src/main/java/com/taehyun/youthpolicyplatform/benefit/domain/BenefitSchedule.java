package com.taehyun.youthpolicyplatform.benefit.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 정책 신청 일정을 저장하는 엔티티
@Getter
@NoArgsConstructor
@Entity
public class BenefitSchedule {

    // 일정 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신청 시작일
    private LocalDate startDate;

    // 신청 종료일
    private LocalDate endDate;

    // 일정이 속한 정책
    @ManyToOne
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;
}
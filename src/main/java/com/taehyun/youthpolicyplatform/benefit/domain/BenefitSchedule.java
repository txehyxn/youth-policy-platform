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

    // 일정 제목
    private String title;

    // 신청 시작일
    private LocalDate startDate;

    // 신청 종료일
    private LocalDate endDate;

    // 일정 설명
    @Column(length = 1000)
    private String description;

    // 일정이 속한 정책
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;

    public BenefitSchedule(
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            Benefit benefit
    ) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.benefit = benefit;
    }

    // 일정 수정
    public void update(
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String description
    ) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }
}
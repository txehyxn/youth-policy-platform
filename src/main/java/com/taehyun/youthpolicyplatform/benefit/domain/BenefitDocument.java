package com.taehyun.youthpolicyplatform.benefit.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 정책 신청에 필요한 서류를 저장하는 엔티티
@Getter
@NoArgsConstructor
@Entity
public class BenefitDocument {

    // 필요 서류 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 서류명
    private String documentName;

    // 서류가 필요한 정책
    @ManyToOne
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;
}
package com.taehyun.youthpolicyplatform.benefit.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 정책 카테고리 정보를 저장하는 엔티티
@Getter
@NoArgsConstructor
@Entity
public class BenefitCategory {

    // 카테고리 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카테고리명
    private String name;

    // 카테고리에 속한 정책 목록
    @OneToMany(mappedBy = "category")
    private List<Benefit> benefits = new ArrayList<>();
}
package com.taehyun.youthpolicyplatform.benefit.domain;

import com.taehyun.youthpolicyplatform.bookmark.domain.Bookmark;
import com.taehyun.youthpolicyplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 청년 정책 및 혜택의 기본 정보를 저장하는 엔티티
@Getter
@NoArgsConstructor
@Entity
public class Benefit extends BaseTimeEntity {

    // 정책 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 정책명
    private String title;

    // 정책 설명
    private String description;

    // 지원 내용
    private String supportAmount;

    // 신청 링크
    private String applicationUrl;

    // 정책이 속한 카테고리
    @ManyToOne
    @JoinColumn(name = "category_id")
    private BenefitCategory category;

    // 정책에 연결된 신청 조건 목록
    @OneToMany(mappedBy = "benefit")
    private List<BenefitCondition> conditions = new ArrayList<>();

    // 정책 신청 일정 목록
    @OneToMany(mappedBy = "benefit")
    private List<BenefitSchedule> schedules = new ArrayList<>();

    // 정책 신청에 필요한 서류 목록
    @OneToMany(mappedBy = "benefit")
    private List<BenefitDocument> documents = new ArrayList<>();

    // 이 정책을 저장한 사용자 목록
    @OneToMany(mappedBy = "benefit")
    private List<Bookmark> bookmarks = new ArrayList<>();
}
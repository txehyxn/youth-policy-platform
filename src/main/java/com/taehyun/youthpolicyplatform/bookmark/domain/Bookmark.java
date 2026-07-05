package com.taehyun.youthpolicyplatform.bookmark.domain;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.common.entity.BaseTimeEntity;
import com.taehyun.youthpolicyplatform.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사용자가 관심 등록한 정책 정보를 저장하는 엔티티
@Getter
@NoArgsConstructor
@Entity
public class Bookmark extends BaseTimeEntity {

    // 관심 등록 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관심 정책을 등록한 사용자
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 관심 등록한 정책
    @ManyToOne
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;

    // 관심 정책 생성자
    public Bookmark(User user, Benefit benefit) {
        this.user = user;
        this.benefit = benefit;
    }
}
package com.taehyun.youthpolicyplatform.user.domain;

import com.taehyun.youthpolicyplatform.bookmark.domain.Bookmark;
import com.taehyun.youthpolicyplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 회원 정보를 저장하는 엔티티
@Getter
@NoArgsConstructor
@Entity
public class User extends BaseTimeEntity {

    // 회원 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 이메일
    private String email;

    // 암호화된 비밀번호
    private String password;

    // 사용자 권한(USER, ADMIN, SUPER_ADMIN)
    @Enumerated(EnumType.STRING)
    private Role role;

    // 회원의 정책 판별용 프로필 정보
    @OneToOne(mappedBy = "user")
    private UserProfile userProfile;

    // 사용자가 저장한 관심 정책 목록
    @OneToMany(mappedBy = "user")
    private List<Bookmark> bookmarks = new ArrayList<>();

    // 사용자 생성자
    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
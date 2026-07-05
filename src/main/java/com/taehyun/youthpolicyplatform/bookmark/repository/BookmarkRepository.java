package com.taehyun.youthpolicyplatform.bookmark.repository;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.bookmark.domain.Bookmark;
import com.taehyun.youthpolicyplatform.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 관심 정책 데이터 접근 Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 특정 사용자가 특정 정책을 관심 등록했는지 조회
    Optional<Bookmark> findByUserAndBenefit(User user, Benefit benefit);

    // 특정 사용자의 관심 정책 목록 조회
    List<Bookmark> findByUser(User user);

    // 특정 사용자가 특정 정책을 관심 등록했는지 여부 확인
    boolean existsByUserAndBenefit(User user, Benefit benefit);
}
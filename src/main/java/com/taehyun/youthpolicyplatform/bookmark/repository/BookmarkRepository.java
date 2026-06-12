package com.taehyun.youthpolicyplatform.bookmark.repository;

import com.taehyun.youthpolicyplatform.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

// 관심 정책 데이터 접근 Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

}
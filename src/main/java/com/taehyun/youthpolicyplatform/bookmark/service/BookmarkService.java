package com.taehyun.youthpolicyplatform.bookmark.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.bookmark.domain.Bookmark;
import com.taehyun.youthpolicyplatform.bookmark.repository.BookmarkRepository;
import com.taehyun.youthpolicyplatform.user.domain.User;
import com.taehyun.youthpolicyplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final BenefitRepository benefitRepository;

    // 관심 정책 등록
    public void addBookmark(String email, Long benefitId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        if (bookmarkRepository.existsByUserAndBenefit(user, benefit)) {
            return;
        }

        Bookmark bookmark = new Bookmark(user, benefit);

        bookmarkRepository.save(bookmark);
    }

    // 관심 정책 취소
    public void removeBookmark(String email, Long benefitId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        Bookmark bookmark = bookmarkRepository.findByUserAndBenefit(user, benefit)
                .orElseThrow(() -> new IllegalArgumentException("관심 등록된 정책이 아닙니다."));

        bookmarkRepository.delete(bookmark);
    }

    // 관심 등록 여부 확인
    public boolean isBookmarked(String email, Long benefitId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        return bookmarkRepository.existsByUserAndBenefit(user, benefit);
    }

    // 내 관심 정책 목록 조회
    public List<Bookmark> findMyBookmarks(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return bookmarkRepository.findByUser(user);
    }
}
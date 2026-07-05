package com.taehyun.youthpolicyplatform.bookmark.controller;

import com.taehyun.youthpolicyplatform.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 관심 정책 등록
    @PostMapping("/bookmarks/{benefitId}")
    public String addBookmark(
            @PathVariable Long benefitId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        bookmarkService.addBookmark(authentication.getName(), benefitId);

        return "redirect:/benefits/" + benefitId;
    }

    // 관심 정책 취소
    @PostMapping("/bookmarks/{benefitId}/delete")
    public String removeBookmark(
            @PathVariable Long benefitId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        bookmarkService.removeBookmark(authentication.getName(), benefitId);

        return "redirect:/benefits/" + benefitId;
    }
}
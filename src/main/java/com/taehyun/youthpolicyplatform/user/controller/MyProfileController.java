package com.taehyun.youthpolicyplatform.user.controller;

import com.taehyun.youthpolicyplatform.bookmark.service.BookmarkService;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MyProfileController {

    private final UserProfileService userProfileService;
    private final BookmarkService bookmarkService;

    @GetMapping("/my/profile")
    public String profileForm(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        try {
            UserProfile profile =
                    userProfileService.findByUserEmail(authentication.getName());

            model.addAttribute("profile", profile);
            model.addAttribute("hasProfile", true);

        } catch (IllegalArgumentException e) {
            model.addAttribute("hasProfile", false);
        }

        model.addAttribute(
                "bookmarks",
                bookmarkService.findMyBookmarks(authentication.getName())
        );

        return "user/my-profile";
    }

    @PostMapping("/my/profile")
    public String saveProfile(
            Authentication authentication,
            @RequestParam Integer age,
            @RequestParam String address,
            @RequestParam Integer householdSize,
            @RequestParam Integer monthlyIncome,
            @RequestParam Integer annualIncome,
            @RequestParam Integer middleIncomePercent,
            @RequestParam Boolean employed,
            @RequestParam Boolean student,
            @RequestParam Boolean houseOwner
    ) {

        userProfileService.saveForLoggedInUser(
                authentication.getName(),
                age,
                address,
                householdSize,
                monthlyIncome,
                annualIncome,
                middleIncomePercent,
                employed,
                student,
                houseOwner
        );

        return "redirect:/my/profile?saved=true";
    }
}
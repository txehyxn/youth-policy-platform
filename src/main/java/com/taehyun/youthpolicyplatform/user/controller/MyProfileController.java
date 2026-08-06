package com.taehyun.youthpolicyplatform.user.controller;

import com.taehyun.youthpolicyplatform.bookmark.service.BookmarkService;
import com.taehyun.youthpolicyplatform.user.domain.EducationStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentStatus;
import com.taehyun.youthpolicyplatform.user.domain.HousingOwnershipStatus;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.dto.UserProfileRequest;
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
    public String profileForm(
            Authentication authentication,
            @RequestParam(required = false) Long returnBenefitId,
            Model model
    ) {

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
        model.addAttribute("returnBenefitId", returnBenefitId);
        model.addAttribute("employmentStatuses", EmploymentStatus.values());
        model.addAttribute("educationStatuses", EducationStatus.values());
        model.addAttribute("housingOwnershipStatuses", HousingOwnershipStatus.values());

        return "user/my-profile";
    }

    @PostMapping("/my/profile")
    public String saveProfile(
            Authentication authentication,
            @ModelAttribute UserProfileRequest profileRequest,
            @RequestParam(required = false) Long returnBenefitId
    ) {

        userProfileService.saveProfileInputsForLoggedInUser(
                authentication.getName(),
                profileRequest
        );

        if (returnBenefitId != null) {
            return "redirect:/benefits/" + returnBenefitId + "?profileSaved=true";
        }

        return "redirect:/my/profile?saved=true";
    }
}

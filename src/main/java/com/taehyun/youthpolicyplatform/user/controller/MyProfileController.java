package com.taehyun.youthpolicyplatform.user.controller;

import com.taehyun.youthpolicyplatform.bookmark.service.BookmarkService;
import com.taehyun.youthpolicyplatform.eligibility.dto.ProfileEligibilityUpdateResponse;
import com.taehyun.youthpolicyplatform.eligibility.service.ProfileEligibilityUpdateService;
import com.taehyun.youthpolicyplatform.user.domain.EducationStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentStatus;
import com.taehyun.youthpolicyplatform.user.domain.HousingOwnershipStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentType;
import com.taehyun.youthpolicyplatform.user.domain.JobSeekingStatus;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.dto.UserProfileRequest;
import com.taehyun.youthpolicyplatform.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyProfileController {

    private final UserProfileService userProfileService;
    private final BookmarkService bookmarkService;
    private final ProfileEligibilityUpdateService profileEligibilityUpdateService;

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

        UserProfile profile = null;

        try {
            profile = userProfileService.findByUserEmail(authentication.getName());
        } catch (IllegalArgumentException e) {
            model.addAttribute("hasProfile", false);
        }

        ProfileEligibilityUpdateResponse eligibilityResponse = null;
        if (profile != null) {
            model.addAttribute("profile", profile);
            model.addAttribute("hasProfile", true);
            eligibilityResponse = profileEligibilityUpdateService.recalculate(profile);
        }

        ProfileEligibilityUpdateResponse.Summary eligibilitySummary =
                eligibilityResponse == null
                        ? new ProfileEligibilityUpdateResponse.Summary(0, 0, 0)
                        : eligibilityResponse.summary();
        List<ProfileEligibilityUpdateResponse.MissingFieldSummary> missingSummaries =
                eligibilityResponse == null
                        ? List.of()
                        : eligibilityResponse.missingFieldSummaries().stream().limit(2).toList();
        model.addAttribute("eligibilitySummary", eligibilitySummary);
        model.addAttribute("missingFieldSummaries", missingSummaries);
        model.addAttribute(
                "topMissingFieldNames",
                missingSummaries.stream().map(summary -> summary.field().name()).toList()
        );

        model.addAttribute(
                "bookmarks",
                bookmarkService.findMyBookmarks(authentication.getName())
        );
        model.addAttribute("returnBenefitId", returnBenefitId);
        model.addAttribute("employmentStatuses", EmploymentStatus.values());
        model.addAttribute("educationStatuses", EducationStatus.values());
        model.addAttribute("housingOwnershipStatuses", HousingOwnershipStatus.values());
        model.addAttribute("employmentTypes", EmploymentType.values());
        model.addAttribute("jobSeekingStatuses", JobSeekingStatus.values());

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

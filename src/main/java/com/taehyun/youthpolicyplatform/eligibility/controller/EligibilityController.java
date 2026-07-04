package com.taehyun.youthpolicyplatform.eligibility.controller;

import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.service.EligibilityService;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;
    private final UserProfileService userProfileService;

    @GetMapping("/eligibility/check")
    public String check(
            @RequestParam Long benefitId,
            Authentication authentication,
            Model model
    ) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        try {
            UserProfile profile =
                    userProfileService.findByUserEmail(authentication.getName());

            EligibilityResultDto result =
                    eligibilityService.check(benefitId, profile.getId());

            model.addAttribute("result", result);

            return "eligibility/result";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "신청 가능 여부를 확인하려면 먼저 내 프로필을 등록해야 합니다.");
            model.addAttribute("benefitId", benefitId);

            return "eligibility/profile-required";
        }
    }
}
package com.taehyun.youthpolicyplatform.eligibility.controller;

import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;

    @GetMapping("/eligibility/check")
    public String check(
            @RequestParam Long benefitId,
            @RequestParam Long profileId,
            Model model
    ) {

        EligibilityResultDto result =
                eligibilityService.check(benefitId, profileId);

        model.addAttribute("result", result);

        return "eligibility/result";
    }
}
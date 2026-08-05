package com.taehyun.youthpolicyplatform.benefit.controller;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitSchedule;
import com.taehyun.youthpolicyplatform.benefit.dto.ConditionDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.dto.ScheduleDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.service.BenefitService;
import com.taehyun.youthpolicyplatform.benefit.util.ConditionDisplayUtil;
import com.taehyun.youthpolicyplatform.benefit.util.ScheduleDisplayUtil;
import com.taehyun.youthpolicyplatform.bookmark.service.BookmarkService;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityStatus;
import com.taehyun.youthpolicyplatform.eligibility.service.EligibilityService;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BenefitUserController {

    private final BenefitService benefitService;
    private final BookmarkService bookmarkService;
    private final EligibilityService eligibilityService;
    private final UserProfileService userProfileService;

    @GetMapping("/benefits")
    public String benefitList(Authentication authentication, Model model) {

        List<Benefit> benefits = benefitService.findAllForEligibility();
        model.addAttribute("benefits", benefits);

        if (!isLoggedIn(authentication)) {
            model.addAttribute("loginRequired", true);
            return "benefit/list";
        }

        UserProfile profile;

        try {
            profile = userProfileService.findByUserEmail(authentication.getName());
        } catch (IllegalArgumentException e) {
            model.addAttribute("profileRequired", true);
            return "benefit/list";
        }

        Map<Long, EligibilityResultDto> eligibilityResults =
                eligibilityService.checkAll(benefits, profile);

        List<Benefit> sortedBenefits = new ArrayList<>(benefits);
        sortedBenefits.sort(Comparator.comparingInt(
                benefit -> statusPriority(eligibilityResults.get(benefit.getId()).getStatus())
        ));

        model.addAttribute("benefits", sortedBenefits);
        model.addAttribute("eligibilityResults", eligibilityResults);
        model.addAttribute("personalized", true);

        return "benefit/list";
    }

    @GetMapping("/benefits/{id}")
    public String benefitDetail(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {

        Benefit benefit = benefitService.findById(id);

        List<ConditionDisplayDto> displayConditions =
                benefit.getConditions()
                        .stream()
                        .map(ConditionDisplayUtil::convert)
                        .toList();

        // 일정을 시작일 순서로 정렬하고, 상태 라벨(신청 예정/신청중/신청 마감)을 붙인다
        List<ScheduleDisplayDto> schedules =
                benefit.getSchedules()
                        .stream()
                        .sorted(Comparator.comparing(
                                BenefitSchedule::getStartDate,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                        .map(ScheduleDisplayUtil::convert)
                        .toList();

        boolean isBookmarked = false;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            isBookmarked = bookmarkService.isBookmarked(authentication.getName(), id);
        }

        model.addAttribute("benefit", benefit);
        model.addAttribute("displayConditions", displayConditions);
        model.addAttribute("schedules", schedules);
        model.addAttribute("isBookmarked", isBookmarked);

        return "benefit/detail";
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    private int statusPriority(EligibilityStatus status) {
        return switch (status) {
            case ELIGIBLE -> 0;
            case NEED_MORE_INFO -> 1;
            case INELIGIBLE -> 2;
        };
    }
}

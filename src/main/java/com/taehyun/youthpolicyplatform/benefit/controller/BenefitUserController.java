package com.taehyun.youthpolicyplatform.benefit.controller;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.dto.ConditionDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.dto.ScheduleDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.service.BenefitService;
import com.taehyun.youthpolicyplatform.benefit.util.ConditionDisplayUtil;
import com.taehyun.youthpolicyplatform.benefit.util.ScheduleDisplayUtil;
import com.taehyun.youthpolicyplatform.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BenefitUserController {

    private final BenefitService benefitService;
    private final BookmarkService bookmarkService;

    @GetMapping("/benefits")
    public String benefitList(Model model) {

        model.addAttribute("benefits", benefitService.findAll());

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
                        .sorted(Comparator.comparing(schedule -> schedule.getStartDate()))
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
}
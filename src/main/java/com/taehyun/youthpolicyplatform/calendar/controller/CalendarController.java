package com.taehyun.youthpolicyplatform.calendar.controller;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.service.BenefitService;
import com.taehyun.youthpolicyplatform.bookmark.domain.Bookmark;
import com.taehyun.youthpolicyplatform.bookmark.service.BookmarkService;
import com.taehyun.youthpolicyplatform.calendar.dto.CalendarEventDto;
import com.taehyun.youthpolicyplatform.calendar.util.CalendarEventUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.taehyun.youthpolicyplatform.benefit.service.BenefitCategoryService;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final BenefitCategoryService benefitCategoryService;
    private final BenefitService benefitService;
    private final BookmarkService bookmarkService;

    // ① 전체 캘린더 "화면"을 보여주는 메서드 (빈 달력 틀만 그려줌)
    @GetMapping("/calendar")
    public String calendarPage(Model model) {

        model.addAttribute("categories", benefitCategoryService.findAll());

        return "calendar/calendar";
    }

    // ② 전체 캘린더에 채워질 "일정 데이터(JSON)"를 주는 메서드
    //    categoryId를 넘기면 그 카테고리만 걸러서 보여준다 (선택사항)
    @GetMapping("/api/calendar/events")
    @ResponseBody
    public List<CalendarEventDto> calendarEvents(
            @RequestParam(required = false) Long categoryId
    ) {

        List<Benefit> benefits = benefitService.findAll();

        return benefits.stream()
                .filter(benefit ->
                        categoryId == null
                                || benefit.getCategory().getId().equals(categoryId)
                )
                .flatMap(benefit -> benefit.getSchedules().stream())
                .map(CalendarEventUtil::convert)
                .toList();
    }

    // ③ 마이페이지 "내 북마크 캘린더" 화면을 보여주는 메서드
    @GetMapping("/my/bookmarks/calendar")
    public String bookmarkCalendarPage(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        return "calendar/bookmark-calendar";
    }

    // ④ 북마크 캘린더에 채워질 "내가 북마크한 정책들의 일정 데이터(JSON)"를 주는 메서드
    @GetMapping("/api/calendar/bookmark-events")
    @ResponseBody
    public List<CalendarEventDto> bookmarkCalendarEvents(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return List.of();
        }

        List<Bookmark> bookmarks =
                bookmarkService.findMyBookmarks(authentication.getName());

        return bookmarks.stream()
                .map(Bookmark::getBenefit)
                .flatMap(benefit -> benefit.getSchedules().stream())
                .map(CalendarEventUtil::convert)
                .toList();
    }
}
package com.taehyun.youthpolicyplatform.benefit.controller;

import com.taehyun.youthpolicyplatform.benefit.service.BenefitScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class BenefitScheduleController {

    private final BenefitScheduleService benefitScheduleService;

    @PostMapping("/admin/benefits/{benefitId}/schedules")
    public String saveSchedule(
            @PathVariable Long benefitId,
            @RequestParam String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String description,
            @RequestParam(required = false, defaultValue = "false") Boolean alwaysOpen
    ) {
        benefitScheduleService.save(
                benefitId,
                title,
                startDate,
                endDate,
                description,
                alwaysOpen
        );

        return "redirect:/admin/benefits/" + benefitId;
    }

    @PostMapping("/admin/benefits/{benefitId}/schedules/delete/{scheduleId}")
    public String deleteSchedule(
            @PathVariable Long benefitId,
            @PathVariable Long scheduleId
    ) {
        benefitScheduleService.delete(scheduleId);

        return "redirect:/admin/benefits/" + benefitId;
    }
}
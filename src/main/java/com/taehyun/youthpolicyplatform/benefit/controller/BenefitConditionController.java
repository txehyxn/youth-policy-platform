package com.taehyun.youthpolicyplatform.benefit.controller;

import com.taehyun.youthpolicyplatform.benefit.service.BenefitConditionService;
import com.taehyun.youthpolicyplatform.benefit.service.BenefitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class BenefitConditionController {

    private final BenefitConditionService benefitConditionService;
    private final BenefitService benefitService;

    @GetMapping("/admin/conditions")
    public String conditionList(Model model) {
        model.addAttribute("conditions", benefitConditionService.findAll());
        model.addAttribute("benefits", benefitService.findAll());

        return "admin/condition-list";
    }

    @PostMapping("/admin/conditions")
    public String saveCondition(
            @RequestParam Long benefitId,
            @RequestParam String fieldName,
            @RequestParam String operator,
            @RequestParam String value,
            @RequestParam Boolean required
    ) {
        benefitConditionService.save(
                benefitId,
                fieldName,
                operator,
                value,
                required
        );

        return "redirect:/admin/conditions";
    }
}
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

    @PostMapping("/admin/conditions/delete/{id}")
    public String deleteCondition(@PathVariable Long id) {

        benefitConditionService.delete(id);

        return "redirect:/admin/conditions";
    }

    // ===== 정책 상세 페이지에서 쓰는 친화적 등록 폼 (신규) =====

    // 나이 조건 등록
    @PostMapping("/admin/benefits/{benefitId}/conditions/age")
    public String saveAgeCondition(
            @PathVariable Long benefitId,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(defaultValue = "true") Boolean required
    ) {
        benefitConditionService.saveAgeRange(benefitId, minAge, maxAge, required);

        return "redirect:/admin/benefits/" + benefitId;
    }

    // 지역 조건 등록
    @PostMapping("/admin/benefits/{benefitId}/conditions/region")
    public String saveRegionCondition(
            @PathVariable Long benefitId,
            @RequestParam String region,
            @RequestParam(defaultValue = "true") Boolean required
    ) {
        benefitConditionService.saveRegion(benefitId, region, required);

        return "redirect:/admin/benefits/" + benefitId;
    }

    // 중위소득 조건 등록
    @PostMapping("/admin/benefits/{benefitId}/conditions/middle-income")
    public String saveMiddleIncomeCondition(
            @PathVariable Long benefitId,
            @RequestParam Integer maxPercent,
            @RequestParam(defaultValue = "true") Boolean required
    ) {
        benefitConditionService.saveMiddleIncomePercent(benefitId, maxPercent, required);

        return "redirect:/admin/benefits/" + benefitId;
    }

    // 취업/학생/자가보유 조건 등록 (O, X 버튼)
    @PostMapping("/admin/benefits/{benefitId}/conditions/boolean")
    public String saveBooleanCondition(
            @PathVariable Long benefitId,
            @RequestParam String fieldName,
            @RequestParam Boolean value,
            @RequestParam(defaultValue = "true") Boolean required
    ) {
        benefitConditionService.saveBooleanCondition(benefitId, fieldName, value, required);

        return "redirect:/admin/benefits/" + benefitId;
    }

    // 조건 삭제 (정책 상세 페이지에서)
    @PostMapping("/admin/benefits/{benefitId}/conditions/delete/{conditionId}")
    public String deleteConditionFromBenefitDetail(
            @PathVariable Long benefitId,
            @PathVariable Long conditionId
    ) {
        benefitConditionService.delete(conditionId);

        return "redirect:/admin/benefits/" + benefitId;
    }
}
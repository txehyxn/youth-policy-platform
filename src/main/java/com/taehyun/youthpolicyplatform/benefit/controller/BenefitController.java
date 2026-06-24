package com.taehyun.youthpolicyplatform.benefit.controller;

import com.taehyun.youthpolicyplatform.benefit.service.BenefitCategoryService;
import com.taehyun.youthpolicyplatform.benefit.service.BenefitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitService benefitService;
    private final BenefitCategoryService benefitCategoryService;

    @GetMapping("/admin/benefits")
    public String benefitList(Model model) {
        model.addAttribute("benefits", benefitService.findAll());
        model.addAttribute("categories", benefitCategoryService.findAll());

        return "admin/benefit-list";
    }

    @PostMapping("/admin/benefits")
    public String saveBenefit(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String supportAmount,
            @RequestParam String applicationUrl,
            @RequestParam Long categoryId
    ) {
        benefitService.save(title, description, supportAmount, applicationUrl, categoryId);

        return "redirect:/admin/benefits";
    }

    @GetMapping("/admin/benefits/{id}")
    public String detail(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "benefit",
                benefitService.findById(id)
        );

        return "admin/benefit-detail";
    }

    @PostMapping("/admin/benefits/delete/{id}")
    public String deleteBenefit(@PathVariable Long id) {

        benefitService.delete(id);

        return "redirect:/admin/benefits";
    }
}
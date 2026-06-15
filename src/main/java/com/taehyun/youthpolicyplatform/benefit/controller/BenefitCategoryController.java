package com.taehyun.youthpolicyplatform.benefit.controller;

import com.taehyun.youthpolicyplatform.benefit.service.BenefitCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BenefitCategoryController {

    private final BenefitCategoryService benefitCategoryService;

    @GetMapping("/admin/categories")
    public String categoryList(Model model) {

        model.addAttribute(
                "categories",
                benefitCategoryService.findAll()
        );

        return "admin/category-list";
    }

    @PostMapping("/admin/categories")
    public String saveCategory(@RequestParam String name) {

        benefitCategoryService.save(name);

        return "redirect:/admin/categories";
    }
}
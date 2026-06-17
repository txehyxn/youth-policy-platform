package com.taehyun.youthpolicyplatform.user.controller;

import com.taehyun.youthpolicyplatform.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/admin/user-profiles")
    public String profileList(Model model) {
        model.addAttribute("profiles", userProfileService.findAll());

        return "admin/user-profile-list";
    }

    @PostMapping("/admin/user-profiles")
    public String saveProfile(
            @RequestParam Integer age,
            @RequestParam String region,
            @RequestParam Integer householdSize,
            @RequestParam Integer monthlyIncome,
            @RequestParam Integer annualIncome,
            @RequestParam Integer middleIncomePercent,
            @RequestParam Boolean employed,
            @RequestParam Boolean student,
            @RequestParam Boolean houseOwner,
            @RequestParam Long userId
    ) {
        userProfileService.save(
                age,
                region,
                householdSize,
                monthlyIncome,
                annualIncome,
                middleIncomePercent,
                employed,
                student,
                houseOwner,
                userId
        );

        return "redirect:/admin/user-profiles";
    }
}
package com.taehyun.youthpolicyplatform.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            model.addAttribute("loginEmail", authentication.getName());

            // 로그인한 사람이 관리자 권한(ADMIN 또는 SUPER_ADMIN)을 가지고 있는지 확인
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN"));

            model.addAttribute("isAdmin", isAdmin);
        }

        return "index";
    }
}
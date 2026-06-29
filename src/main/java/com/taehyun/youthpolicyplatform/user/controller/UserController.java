package com.taehyun.youthpolicyplatform.user.controller;

import com.taehyun.youthpolicyplatform.user.dto.SignupRequest;
import com.taehyun.youthpolicyplatform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 화면
    @GetMapping("/signup")
    public String signupForm() {
        return "user/signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(SignupRequest request) {

        userService.signup(request);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }
}
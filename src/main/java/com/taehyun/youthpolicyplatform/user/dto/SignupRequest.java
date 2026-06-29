package com.taehyun.youthpolicyplatform.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String email;
    private String password;
    private String passwordConfirm;
}
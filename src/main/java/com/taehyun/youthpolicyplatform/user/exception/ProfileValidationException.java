package com.taehyun.youthpolicyplatform.user.exception;

import lombok.Getter;

@Getter
public class ProfileValidationException extends RuntimeException {

    private final String field;

    public ProfileValidationException(String field, String message) {
        super(message);
        this.field = field;
    }
}

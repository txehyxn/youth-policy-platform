package com.taehyun.youthpolicyplatform.user.controller;

import com.taehyun.youthpolicyplatform.user.dto.ProfileApiErrorResponse;
import com.taehyun.youthpolicyplatform.user.exception.ProfileValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MyProfileApiController.class)
public class ProfileApiExceptionHandler {

    @ExceptionHandler(ProfileValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProfileApiErrorResponse handleValidation(ProfileValidationException exception) {
        return new ProfileApiErrorResponse(
                "INVALID_PROFILE_FIELD", exception.getMessage(), exception.getField()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProfileApiErrorResponse handleUnreadableRequest(
            HttpMessageNotReadableException exception
    ) {
        return new ProfileApiErrorResponse(
                "INVALID_REQUEST_VALUE",
                "요청 값의 형식 또는 enum 값이 올바르지 않습니다.",
                null
        );
    }
}

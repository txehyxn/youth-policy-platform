package com.taehyun.youthpolicyplatform.user.dto;

public record ProfileApiErrorResponse(
        String code,
        String message,
        String field
) {
}

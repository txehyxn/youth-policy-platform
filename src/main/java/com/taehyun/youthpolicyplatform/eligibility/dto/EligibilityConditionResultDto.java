package com.taehyun.youthpolicyplatform.eligibility.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EligibilityConditionResultDto {

    // 원본 데이터
    private String fieldName;
    private String operator;
    private String value;

    // 화면 표시용 정책 조건
    private String fieldLabel;
    private String operatorLabel;
    private String valueLabel;

    // 내 정보(화면 표시용)
    private String userValueLabel;

    // 판별 결과
    private boolean passed;

    // 안내 메시지
    private String message;
}
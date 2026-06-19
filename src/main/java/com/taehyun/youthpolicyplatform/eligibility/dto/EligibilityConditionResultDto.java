package com.taehyun.youthpolicyplatform.eligibility.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EligibilityConditionResultDto {

    private String fieldName;
    private String operator;
    private String value;
    private boolean passed;
}
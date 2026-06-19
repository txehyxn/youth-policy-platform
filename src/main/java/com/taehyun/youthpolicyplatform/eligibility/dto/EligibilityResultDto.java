package com.taehyun.youthpolicyplatform.eligibility.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EligibilityResultDto {

    private String benefitTitle;
    private boolean eligible;
    private List<EligibilityConditionResultDto> conditionResults;
}
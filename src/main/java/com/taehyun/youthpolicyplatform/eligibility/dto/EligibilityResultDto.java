package com.taehyun.youthpolicyplatform.eligibility.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class EligibilityResultDto {

    private String benefitTitle;
    private EligibilityStatus status;

    // 기존 화면과 응답 구조의 호환성을 위해 유지한다.
    private boolean eligible;
    private List<EligibilityConditionResultDto> conditionResults;

    public EligibilityResultDto(
            String benefitTitle,
            EligibilityStatus status,
            List<EligibilityConditionResultDto> conditionResults
    ) {
        this.benefitTitle = benefitTitle;
        this.status = status;
        this.eligible = status == EligibilityStatus.ELIGIBLE;
        this.conditionResults = conditionResults;
    }

    public boolean isIneligible() {
        return status == EligibilityStatus.INELIGIBLE;
    }

    public boolean isNeedMoreInfo() {
        return status == EligibilityStatus.NEED_MORE_INFO;
    }
}

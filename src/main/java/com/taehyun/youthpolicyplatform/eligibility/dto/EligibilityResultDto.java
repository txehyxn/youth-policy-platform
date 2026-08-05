package com.taehyun.youthpolicyplatform.eligibility.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class EligibilityResultDto {

    private Long benefitId;
    private String benefitTitle;
    private String categoryName;
    private String supportAmount;
    private String applicationUrl;
    private EligibilityStatus status;

    // 기존 화면과 응답 구조의 호환성을 위해 유지한다.
    private boolean eligible;
    private List<EligibilityConditionResultDto> conditionResults;

    public EligibilityResultDto(
            String benefitTitle,
            EligibilityStatus status,
            List<EligibilityConditionResultDto> conditionResults
    ) {
        this(null, benefitTitle, "", null, null, status, conditionResults);
    }

    public EligibilityResultDto(
            Long benefitId,
            String benefitTitle,
            String categoryName,
            String supportAmount,
            String applicationUrl,
            EligibilityStatus status,
            List<EligibilityConditionResultDto> conditionResults
    ) {
        this.benefitId = benefitId;
        this.benefitTitle = benefitTitle;
        this.categoryName = categoryName;
        this.supportAmount = supportAmount;
        this.applicationUrl = applicationUrl;
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

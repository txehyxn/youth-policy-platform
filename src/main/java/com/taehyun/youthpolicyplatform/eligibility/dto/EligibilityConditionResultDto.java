package com.taehyun.youthpolicyplatform.eligibility.dto;

import lombok.Getter;

@Getter
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
    private EligibilityStatus status;

    // 기존 화면과 응답 구조의 호환성을 위해 유지한다.
    private boolean passed;

    // 안내 메시지
    private String message;

    public EligibilityConditionResultDto(
            String fieldName,
            String operator,
            String value,
            String fieldLabel,
            String operatorLabel,
            String valueLabel,
            String userValueLabel,
            EligibilityStatus status,
            String message
    ) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
        this.fieldLabel = fieldLabel;
        this.operatorLabel = operatorLabel;
        this.valueLabel = valueLabel;
        this.userValueLabel = userValueLabel;
        this.status = status;
        this.passed = status == EligibilityStatus.ELIGIBLE;
        this.message = message;
    }

    public boolean isNeedMoreInfo() {
        return status == EligibilityStatus.NEED_MORE_INFO;
    }
}

package com.taehyun.youthpolicyplatform.user.domain;

public enum EmploymentStatus {
    UNEMPLOYED("미취업"),
    EMPLOYED("취업 중"),
    SELF_EMPLOYED("자영업/사업 중"),
    NOT_ECONOMICALLY_ACTIVE("비경제활동 상태");

    private final String label;

    EmploymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

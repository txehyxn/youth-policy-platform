package com.taehyun.youthpolicyplatform.user.domain;

public enum HousingOwnershipStatus {
    NO_HOME("무주택"),
    APPLICANT_OWNS("본인 명의 주택 보유"),
    HOUSEHOLD_MEMBER_OWNS("가구원 주택 보유");

    private final String label;

    HousingOwnershipStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

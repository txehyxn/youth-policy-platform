package com.taehyun.youthpolicyplatform.user.domain;

public enum EmploymentType {
    FULL_TIME("정규직"),
    PART_TIME("시간제·파트타임"),
    CONTRACT("계약직"),
    DAILY("일용직"),
    PLATFORM("플랫폼 노동"),
    OTHER("기타");

    private final String label;

    EmploymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

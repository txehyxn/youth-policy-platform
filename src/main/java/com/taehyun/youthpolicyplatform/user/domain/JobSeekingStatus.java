package com.taehyun.youthpolicyplatform.user.domain;

public enum JobSeekingStatus {
    REGISTERED("구직 등록 중"),
    SEEKING_NOT_REGISTERED("구직 중이지만 등록하지 않음"),
    NOT_SEEKING("현재 구직 중이 아님");

    private final String label;

    JobSeekingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

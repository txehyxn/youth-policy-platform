package com.taehyun.youthpolicyplatform.user.domain;

public enum EducationStatus {
    HIGH_SCHOOL_STUDENT("고등학생"),
    UNIVERSITY_ENROLLED("대학 재학"),
    LEAVE_OF_ABSENCE("휴학"),
    EXPECTED_GRADUATION("졸업 예정"),
    GRADUATED("졸업"),
    DROPPED_OUT("중퇴"),
    NOT_APPLICABLE("해당 없음");

    private final String label;

    EducationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

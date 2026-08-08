package com.taehyun.youthpolicyplatform.user.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileFieldTest {

    @Test
    void resolvesCanonicalAndLegacyKeys() {
        assertThat(ProfileField.fromKey("monthlyEarnedIncome"))
                .contains(ProfileField.MONTHLY_EARNED_INCOME);
        assertThat(ProfileField.fromKey("monthlyIncome"))
                .contains(ProfileField.MONTHLY_EARNED_INCOME);
        assertThat(ProfileField.fromKey("address"))
                .contains(ProfileField.REGION);
        assertThat(ProfileField.fromKey("employed"))
                .contains(ProfileField.EMPLOYMENT_STATUS);
        assertThat(ProfileField.fromKey("student"))
                .contains(ProfileField.EDUCATION_STATUS);
        assertThat(ProfileField.fromKey("houseOwner"))
                .contains(ProfileField.HOUSING_OWNERSHIP_STATUS);
        assertThat(ProfileField.fromKey("graduationMonths"))
                .contains(ProfileField.GRADUATION_MONTHS);
        assertThat(ProfileField.fromKey("employmentType"))
                .contains(ProfileField.EMPLOYMENT_TYPE);
        assertThat(ProfileField.fromKey("smeEmployee"))
                .contains(ProfileField.SME_EMPLOYEE);
        assertThat(ProfileField.fromKey("jobSeekingStatus"))
                .contains(ProfileField.JOB_SEEKING_STATUS);
    }

    @Test
    void rejectsUnknownOrBlankKeys() {
        assertThat(ProfileField.fromKey("unknownField")).isEmpty();
        assertThat(ProfileField.fromKey(" ")).isEmpty();
        assertThat(ProfileField.fromKey(null)).isEmpty();
    }
}

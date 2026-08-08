package com.taehyun.youthpolicyplatform.user.domain;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public enum ProfileField {
    BIRTH_DATE("birthDate", "생년월일"),
    AGE("age", "나이"),
    REGION("region", "거주지역", "address"),
    HOUSEHOLD_SIZE("householdSize", "가구원 수"),
    MONTHLY_EARNED_INCOME("monthlyEarnedIncome", "월 근로·사업소득", "monthlyIncome"),
    ANNUAL_PERSONAL_INCOME("annualPersonalIncome", "개인 연소득", "annualIncome"),
    HOUSEHOLD_MONTHLY_INCOME("householdMonthlyIncome", "가구 월소득"),
    MIDDLE_INCOME_PERCENT("middleIncomePercent", "중위소득"),
    EMPLOYMENT_STATUS("employmentStatus", "취업 상태", "employed"),
    EDUCATION_STATUS("educationStatus", "학적 상태", "student"),
    HOUSING_OWNERSHIP_STATUS("housingOwnershipStatus", "주택 소유 상태", "houseOwner"),
    GRADUATION_DATE("graduationDate", "졸업 시점"),
    GRADUATION_MONTHS("graduationMonths", "졸업 후 경과 기간"),
    EMPLOYMENT_TYPE("employmentType", "고용 형태"),
    SME_EMPLOYEE("smeEmployee", "중소기업 재직 여부"),
    JOB_SEEKING_STATUS("jobSeekingStatus", "구직 상태");

    private final String key;
    private final String label;
    private final Set<String> supportedKeys;

    ProfileField(String key, String label, String... legacyKeys) {
        this.key = key;
        this.label = label;
        this.supportedKeys = new LinkedHashSet<>();
        this.supportedKeys.add(key);
        this.supportedKeys.add(name());
        this.supportedKeys.addAll(Arrays.asList(legacyKeys));
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public static Optional<ProfileField> fromKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }

        String trimmedKey = key.trim();

        return Arrays.stream(values())
                .filter(field -> field.supportedKeys.stream()
                        .anyMatch(supportedKey -> supportedKey.equalsIgnoreCase(trimmedKey)))
                .findFirst();
    }
}

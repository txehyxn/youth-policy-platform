package com.taehyun.youthpolicyplatform.eligibility.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.dto.ConditionDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.benefit.util.ConditionDisplayUtil;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityConditionResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityStatus;
import com.taehyun.youthpolicyplatform.user.domain.ProfileField;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentType;
import com.taehyun.youthpolicyplatform.user.domain.JobSeekingStatus;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EligibilityService {

    private final BenefitRepository benefitRepository;
    private final UserProfileRepository userProfileRepository;
    private final Clock clock;

    @Autowired
    public EligibilityService(
            BenefitRepository benefitRepository,
            UserProfileRepository userProfileRepository
    ) {
        this(benefitRepository, userProfileRepository, Clock.systemDefaultZone());
    }

    public EligibilityService(
            BenefitRepository benefitRepository,
            UserProfileRepository userProfileRepository,
            Clock clock
    ) {
        this.benefitRepository = benefitRepository;
        this.userProfileRepository = userProfileRepository;
        this.clock = clock;
    }

    public EligibilityResultDto check(Long benefitId, Long profileId) {

        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));

        return check(benefit, profile);
    }

    public Map<Long, EligibilityResultDto> checkAll(
            List<Benefit> benefits,
            UserProfile profile
    ) {
        Map<Long, EligibilityResultDto> results = new LinkedHashMap<>();

        for (Benefit benefit : benefits) {
            results.put(benefit.getId(), check(benefit, profile));
        }

        return results;
    }

    private EligibilityResultDto check(Benefit benefit, UserProfile profile) {
        List<EligibilityConditionResultDto> conditionResults = new ArrayList<>();

        boolean hasRequiredFailure = false;
        boolean hasRequiredMissingValue = false;

        for (BenefitCondition condition : benefit.getConditions()) {

            EligibilityStatus conditionStatus = evaluateCondition(profile, condition);

            if (Boolean.TRUE.equals(condition.getRequired())) {
                if (conditionStatus == EligibilityStatus.INELIGIBLE) {
                    hasRequiredFailure = true;
                } else if (conditionStatus == EligibilityStatus.NEED_MORE_INFO) {
                    hasRequiredMissingValue = true;
                }
            }

            ConditionDisplayDto displayCondition = ConditionDisplayUtil.convert(condition);

            conditionResults.add(
                    new EligibilityConditionResultDto(
                            condition.getFieldName(),
                            condition.getOperator(),
                            condition.getValue(),

                            displayCondition.getFieldLabel(),
                            displayCondition.getOperatorLabel(),
                            displayCondition.getValueLabel(),

                            createUserValueLabel(profile, condition),

                            conditionStatus,
                            createMessage(condition, conditionStatus)
                    )
            );
        }

        EligibilityStatus status = determineStatus(
                hasRequiredFailure,
                hasRequiredMissingValue
        );

        return new EligibilityResultDto(
                benefit.getId(),
                benefit.getTitle(),
                benefit.getCategory() == null ? "" : benefit.getCategory().getName(),
                benefit.getSupportAmount(),
                benefit.getApplicationUrl(),
                status,
                conditionResults
        );
    }

    private EligibilityStatus determineStatus(
            boolean hasRequiredFailure,
            boolean hasRequiredMissingValue
    ) {
        if (hasRequiredFailure) {
            return EligibilityStatus.INELIGIBLE;
        }
        if (hasRequiredMissingValue) {
            return EligibilityStatus.NEED_MORE_INFO;
        }
        return EligibilityStatus.ELIGIBLE;
    }

    private EligibilityStatus evaluateCondition(
            UserProfile profile,
            BenefitCondition condition
    ) {
        String fieldName = trimToEmpty(condition.getFieldName());

        if (isUserValueMissing(profile, fieldName)) {
            return EligibilityStatus.NEED_MORE_INFO;
        }

        return checkCondition(profile, condition)
                ? EligibilityStatus.ELIGIBLE
                : EligibilityStatus.INELIGIBLE;
    }

    private boolean isUserValueMissing(UserProfile profile, String fieldName) {
        ProfileField profileField = ProfileField.fromKey(fieldName).orElse(null);

        if (profileField == null) {
            return false;
        }

        return switch (profileField) {
            case BIRTH_DATE -> profile.getBirthDate() == null;
            case AGE -> profile.getEligibilityAge() == null;
            case REGION -> isBlank(profile.getRegionCode()) && isBlank(profile.getAddress());
            case HOUSEHOLD_SIZE -> profile.getHouseholdSize() == null;
            case MONTHLY_EARNED_INCOME -> profile.getEligibilityMonthlyEarnedIncome() == null;
            case ANNUAL_PERSONAL_INCOME -> profile.getEligibilityAnnualPersonalIncome() == null;
            case HOUSEHOLD_MONTHLY_INCOME -> profile.getHouseholdMonthlyIncome() == null;
            case MIDDLE_INCOME_PERCENT -> profile.getMiddleIncomePercent() == null;
            case EMPLOYMENT_STATUS -> isLegacyKey(fieldName, "employed")
                    ? profile.getEligibilityEmployed() == null
                    : profile.getEligibilityEmploymentStatus() == null;
            case EDUCATION_STATUS -> isLegacyKey(fieldName, "student")
                    ? profile.getEligibilityStudent() == null
                    : profile.getEligibilityEducationStatus() == null;
            case HOUSING_OWNERSHIP_STATUS -> isLegacyKey(fieldName, "houseOwner")
                    ? profile.getEligibilityHouseOwner() == null
                    : profile.getEligibilityHousingOwnershipStatus() == null;
            case GRADUATION_DATE, GRADUATION_MONTHS -> profile.getGraduationDate() == null;
            case EMPLOYMENT_TYPE -> profile.getEmploymentType() == null;
            case SME_EMPLOYEE -> profile.getSmeEmployee() == null;
            case JOB_SEEKING_STATUS -> profile.getJobSeekingStatus() == null;
        };
    }

    private boolean checkCondition(UserProfile profile, BenefitCondition condition) {

        String fieldName = trimToEmpty(condition.getFieldName());
        String operator = trimToEmpty(condition.getOperator());
        String value = trimToEmpty(condition.getValue());

        if (fieldName.isEmpty() || operator.isEmpty() || value.isEmpty()) {
            return false;
        }

        ProfileField profileField = ProfileField.fromKey(fieldName).orElse(null);

        if (profileField == null) {
            return false;
        }

        return switch (profileField) {
            case BIRTH_DATE -> false;
            case AGE -> compareNumber(profile.getEligibilityAge(), operator, value);
            case REGION -> compareProfileRegion(profile, operator, value);
            case HOUSEHOLD_SIZE -> compareNumber(profile.getHouseholdSize(), operator, value);
            case MONTHLY_EARNED_INCOME -> compareNumber(
                    profile.getEligibilityMonthlyEarnedIncome(), operator, value
            );
            case ANNUAL_PERSONAL_INCOME -> compareNumber(
                    profile.getEligibilityAnnualPersonalIncome(), operator, value
            );
            case HOUSEHOLD_MONTHLY_INCOME -> compareNumber(
                    profile.getHouseholdMonthlyIncome(), operator, value
            );
            case MIDDLE_INCOME_PERCENT -> compareNumber(
                    profile.getMiddleIncomePercent(), operator, value
            );
            case EMPLOYMENT_STATUS -> isLegacyKey(fieldName, "employed")
                    ? compareBoolean(profile.getEligibilityEmployed(), operator, value)
                    : compareEnum(profile.getEligibilityEmploymentStatus(), operator, value);
            case EDUCATION_STATUS -> isLegacyKey(fieldName, "student")
                    ? compareBoolean(profile.getEligibilityStudent(), operator, value)
                    : compareEnum(profile.getEligibilityEducationStatus(), operator, value);
            case HOUSING_OWNERSHIP_STATUS -> isLegacyKey(fieldName, "houseOwner")
                    ? compareBoolean(profile.getEligibilityHouseOwner(), operator, value)
                    : compareEnum(profile.getEligibilityHousingOwnershipStatus(), operator, value);
            case GRADUATION_DATE -> false;
            case GRADUATION_MONTHS -> compareNumber(
                    calculateGraduationMonths(profile.getGraduationDate()), operator, value
            );
            case EMPLOYMENT_TYPE -> compareEnum(profile.getEmploymentType(), operator, value);
            case SME_EMPLOYEE -> compareBoolean(profile.getSmeEmployee(), operator, value);
            case JOB_SEEKING_STATUS -> compareEnum(profile.getJobSeekingStatus(), operator, value);
        };
    }

    private String createUserValueLabel(UserProfile profile, BenefitCondition condition) {

        String fieldName = trimToEmpty(condition.getFieldName());
        ProfileField profileField = ProfileField.fromKey(fieldName).orElse(null);

        if (profileField == null) {
            return "-";
        }

        return switch (profileField) {
            case BIRTH_DATE -> valueOrMissing(profile.getBirthDate());
            case AGE -> valueOrMissing(profile.getEligibilityAge(), "세");
            case REGION -> valueOrMissing(resolveRegionLabel(profile));
            case HOUSEHOLD_SIZE -> valueOrMissing(profile.getHouseholdSize(), "명");
            case MONTHLY_EARNED_INCOME -> valueOrMissing(
                    profile.getEligibilityMonthlyEarnedIncome(), "원"
            );
            case ANNUAL_PERSONAL_INCOME -> valueOrMissing(
                    profile.getEligibilityAnnualPersonalIncome(), "원"
            );
            case HOUSEHOLD_MONTHLY_INCOME -> valueOrMissing(
                    profile.getHouseholdMonthlyIncome(), "원"
            );
            case MIDDLE_INCOME_PERCENT -> valueOrMissing(
                    profile.getMiddleIncomePercent(), "%"
            );
            case EMPLOYMENT_STATUS -> isLegacyKey(fieldName, "employed")
                    ? booleanLabel(profile.getEligibilityEmployed(), "예", "아니오")
                    : enumLabel(profile.getEligibilityEmploymentStatus());
            case EDUCATION_STATUS -> isLegacyKey(fieldName, "student")
                    ? booleanLabel(profile.getEligibilityStudent(), "예", "아니오")
                    : enumLabel(profile.getEligibilityEducationStatus());
            case HOUSING_OWNERSHIP_STATUS -> isLegacyKey(fieldName, "houseOwner")
                    ? booleanLabel(profile.getEligibilityHouseOwner(), "주택 보유", "무주택")
                    : enumLabel(profile.getEligibilityHousingOwnershipStatus());
            case GRADUATION_DATE -> valueOrMissing(profile.getGraduationDate());
            case GRADUATION_MONTHS -> valueOrMissing(
                    calculateGraduationMonths(profile.getGraduationDate()), "개월"
            );
            case EMPLOYMENT_TYPE -> enumLabel(profile.getEmploymentType());
            case SME_EMPLOYEE -> booleanLabel(profile.getSmeEmployee(), "예", "아니요");
            case JOB_SEEKING_STATUS -> enumLabel(profile.getJobSeekingStatus());
        };
    }

    private String createMessage(
            BenefitCondition condition,
            EligibilityStatus status
    ) {
        String fieldName = trimToEmpty(condition.getFieldName());

        if (status == EligibilityStatus.NEED_MORE_INFO) {
            return createMissingValueMessage(fieldName);
        }

        if (status == EligibilityStatus.ELIGIBLE) {
            return createSatisfiedMessage(fieldName);
        }

        return switch (fieldName) {
            case "birthDate" -> "생년월일 조건을 충족하지 않습니다.";
            case "age" -> "나이 조건을 충족하지 않습니다.";
            case "region", "address" -> "거주지역 조건을 충족하지 않습니다.";
            case "householdSize" -> "가구원 수 조건을 충족하지 않습니다.";
            case "monthlyIncome", "monthlyEarnedIncome" -> "월 소득 조건을 충족하지 않습니다.";
            case "annualIncome", "annualPersonalIncome" -> "연 소득 조건을 충족하지 않습니다.";
            case "householdMonthlyIncome" -> "가구 월소득 조건을 충족하지 않습니다.";
            case "middleIncomePercent" -> "중위소득 기준을 충족하지 않습니다.";
            case "employed" -> "취업 여부 조건을 충족하지 않습니다.";
            case "employmentStatus" -> "취업 상태 조건을 충족하지 않습니다.";
            case "student" -> "학생 여부 조건을 충족하지 않습니다.";
            case "educationStatus" -> "학적 상태 조건을 충족하지 않습니다.";
            case "houseOwner" -> "주택 보유 여부 조건을 충족하지 않습니다.";
            case "housingOwnershipStatus" -> "주택 소유 상태 조건을 충족하지 않습니다.";
            case "graduationMonths" -> "졸업 후 경과 기간 조건을 충족하지 않습니다.";
            case "employmentType" -> "고용 형태 조건을 충족하지 않습니다.";
            case "smeEmployee" -> "중소기업 재직 여부 조건을 충족하지 않습니다.";
            case "jobSeekingStatus" -> "구직 상태 조건을 충족하지 않습니다.";
            default -> "조건을 충족하지 않습니다.";
        };
    }

    private String createSatisfiedMessage(String fieldName) {
        return switch (fieldName) {
            case "birthDate" -> "생년월일 조건을 충족했습니다.";
            case "age" -> "나이 조건을 충족했습니다.";
            case "region", "address" -> "거주지역 조건을 충족했습니다.";
            case "householdSize" -> "가구원 수 조건을 충족했습니다.";
            case "monthlyIncome", "monthlyEarnedIncome" -> "월 소득 조건을 충족했습니다.";
            case "annualIncome", "annualPersonalIncome" -> "연 소득 조건을 충족했습니다.";
            case "householdMonthlyIncome" -> "가구 월소득 조건을 충족했습니다.";
            case "middleIncomePercent" -> "중위소득 기준을 충족했습니다.";
            case "employed" -> "취업 여부 조건을 충족했습니다.";
            case "employmentStatus" -> "취업 상태 조건을 충족했습니다.";
            case "student" -> "학생 여부 조건을 충족했습니다.";
            case "educationStatus" -> "학적 상태 조건을 충족했습니다.";
            case "houseOwner" -> "주택 보유 여부 조건을 충족했습니다.";
            case "housingOwnershipStatus" -> "주택 소유 상태 조건을 충족했습니다.";
            case "graduationMonths" -> "졸업 후 경과 기간 조건을 충족했습니다.";
            case "employmentType" -> "고용 형태 조건을 충족했습니다.";
            case "smeEmployee" -> "중소기업 재직 여부 조건을 충족했습니다.";
            case "jobSeekingStatus" -> "구직 상태 조건을 충족했습니다.";
            default -> "조건을 충족했습니다.";
        };
    }

    private String createMissingValueMessage(String fieldName) {
        return switch (fieldName) {
            case "birthDate" -> "생년월일 정보가 없어 확인이 필요합니다.";
            case "age" -> "나이 정보가 없어 확인이 필요합니다.";
            case "region", "address" -> "거주지역 정보가 없어 확인이 필요합니다.";
            case "householdSize" -> "가구원 수 정보가 없어 확인이 필요합니다.";
            case "monthlyIncome", "monthlyEarnedIncome" -> "월 소득 정보가 없어 확인이 필요합니다.";
            case "annualIncome", "annualPersonalIncome" -> "연 소득 정보가 없어 확인이 필요합니다.";
            case "householdMonthlyIncome" -> "가구 월소득 정보가 없어 확인이 필요합니다.";
            case "middleIncomePercent" -> "중위소득 정보가 없어 확인이 필요합니다.";
            case "employed" -> "취업 여부 정보가 없어 확인이 필요합니다.";
            case "employmentStatus" -> "취업 상태 정보가 없어 확인이 필요합니다.";
            case "student" -> "학생 여부 정보가 없어 확인이 필요합니다.";
            case "educationStatus" -> "학적 상태 정보가 없어 확인이 필요합니다.";
            case "houseOwner" -> "주택 보유 여부 정보가 없어 확인이 필요합니다.";
            case "housingOwnershipStatus" -> "주택 소유 상태 정보가 없어 확인이 필요합니다.";
            case "graduationDate", "graduationMonths" -> "졸업 시점 정보가 없어 확인이 필요합니다.";
            case "employmentType" -> "고용 형태 정보가 없어 확인이 필요합니다.";
            case "smeEmployee" -> "중소기업 재직 여부 정보가 없어 확인이 필요합니다.";
            case "jobSeekingStatus" -> "구직 상태 정보가 없어 확인이 필요합니다.";
            default -> "사용자 정보가 없어 확인이 필요합니다.";
        };
    }

    private boolean compareNumber(Number userValue, String operator, String value) {
        long conditionValue;

        try {
            conditionValue = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return false;
        }

        long numericUserValue = userValue.longValue();

        return switch (operator) {
            case ">=" -> numericUserValue >= conditionValue;
            case "<=" -> numericUserValue <= conditionValue;
            case ">" -> numericUserValue > conditionValue;
            case "<" -> numericUserValue < conditionValue;
            case "=", "==" -> numericUserValue == conditionValue;
            case "!=" -> numericUserValue != conditionValue;
            default -> false;
        };
    }

    Long calculateGraduationMonths(LocalDate graduationDate) {
        if (graduationDate == null) {
            return null;
        }
        return ChronoUnit.MONTHS.between(
                graduationDate,
                LocalDate.now(clock)
        );
    }

    private boolean compareProfileRegion(
            UserProfile profile,
            String operator,
            String conditionValue
    ) {
        List<String> regionValues = new ArrayList<>();

        if (!isBlank(profile.getRegionCode())) {
            regionValues.add(profile.getRegionCode());
        }
        if (!isBlank(profile.getAddress())
                && !regionValues.contains(profile.getAddress())) {
            regionValues.add(profile.getAddress());
        }

        if (operator.equals("!=") || operator.equals("NOT_IN")) {
            return regionValues.stream()
                    .allMatch(region -> compareAddress(region, operator, conditionValue));
        }

        return regionValues.stream()
                .anyMatch(region -> compareAddress(region, operator, conditionValue));
    }

    private boolean compareEnum(Enum<?> userValue, String operator, String value) {
        List<String> conditionValues = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(conditionValue -> !conditionValue.isEmpty())
                .toList();

        if (conditionValues.isEmpty()) {
            return false;
        }

        boolean matched = conditionValues.stream()
                .anyMatch(conditionValue -> userValue.name().equalsIgnoreCase(conditionValue));

        return switch (operator) {
            case "=", "==", "IN" -> matched;
            case "!=", "NOT_IN" -> !matched;
            default -> false;
        };
    }

    private boolean compareAddress(String address, String operator, String conditionValue) {

        String normalizedAddress = normalizeRegion(address);

        return switch (operator) {
            case "=", "==" -> matchesRegion(normalizedAddress, conditionValue);
            case "!=" -> !matchesRegion(normalizedAddress, conditionValue);
            case "IN" -> compareRegionList(normalizedAddress, conditionValue, true);
            case "NOT_IN" -> compareRegionList(normalizedAddress, conditionValue, false);
            default -> false;
        };
    }

    private boolean compareRegionList(
            String normalizedAddress,
            String conditionValue,
            boolean expectedToMatch
    ) {
        List<String> regions = Arrays.stream(conditionValue.split(","))
                .map(String::trim)
                .filter(region -> !region.isEmpty())
                .toList();

        if (regions.isEmpty()) {
            return false;
        }

        boolean matched = regions.stream()
                .anyMatch(region -> matchesRegion(normalizedAddress, region));

        return expectedToMatch == matched;
    }

    private boolean matchesRegion(String normalizedAddress, String region) {
        String normalizedRegion = normalizeRegion(region.trim());
        return !normalizedRegion.isEmpty()
                && normalizedAddress.contains(normalizedRegion);
    }

    private String normalizeRegion(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("서울 ", "서울특별시 ")
                .replace("부산 ", "부산광역시 ")
                .replace("대구 ", "대구광역시 ")
                .replace("인천 ", "인천광역시 ")
                .replace("광주 ", "광주광역시 ")
                .replace("대전 ", "대전광역시 ")
                .replace("울산 ", "울산광역시 ")
                .replace("세종 ", "세종특별자치시 ")
                .replace("경기 ", "경기도 ")
                .replace("강원 ", "강원특별자치도 ")
                .replace("충북 ", "충청북도 ")
                .replace("충남 ", "충청남도 ")
                .replace("전북 ", "전북특별자치도 ")
                .replace("전남 ", "전라남도 ")
                .replace("경북 ", "경상북도 ")
                .replace("경남 ", "경상남도 ")
                .replace("제주 ", "제주특별자치도 ");
    }

    private boolean compareBoolean(Boolean userValue, String operator, String value) {
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            return false;
        }

        Boolean conditionValue = Boolean.parseBoolean(value);

        return switch (operator) {
            case "=", "==" -> userValue.equals(conditionValue);
            case "!=" -> !userValue.equals(conditionValue);
            default -> false;
        };
    }

    private String resolveRegionLabel(UserProfile profile) {
        if (!isBlank(profile.getRegionCode())) {
            return profile.getRegionCode();
        }
        return profile.getAddress();
    }

    private String valueOrMissing(Object value) {
        return value == null ? "미입력" : value.toString();
    }

    private String valueOrMissing(Number value, String suffix) {
        return value == null ? "미입력" : value + suffix;
    }

    private String booleanLabel(Boolean value, String trueLabel, String falseLabel) {
        if (value == null) {
            return "미입력";
        }
        return value ? trueLabel : falseLabel;
    }

    private String enumLabel(Enum<?> value) {
        if (value == null) {
            return "미입력";
        }
        if (value instanceof EmploymentType employmentType) {
            return employmentType.getLabel();
        }
        if (value instanceof JobSeekingStatus jobSeekingStatus) {
            return jobSeekingStatus.getLabel();
        }
        return value.name();
    }

    private boolean isLegacyKey(String actualKey, String legacyKey) {
        return legacyKey.equalsIgnoreCase(trimToEmpty(actualKey));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}

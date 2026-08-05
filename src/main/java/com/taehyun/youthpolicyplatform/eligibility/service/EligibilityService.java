package com.taehyun.youthpolicyplatform.eligibility.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.dto.ConditionDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.benefit.util.ConditionDisplayUtil;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityConditionResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityStatus;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final BenefitRepository benefitRepository;
    private final UserProfileRepository userProfileRepository;

    public EligibilityResultDto check(Long benefitId, Long profileId) {

        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));

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
                benefit.getTitle(),
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
        if (isUserValueMissing(profile, condition.getFieldName().trim())) {
            return EligibilityStatus.NEED_MORE_INFO;
        }

        return checkCondition(profile, condition)
                ? EligibilityStatus.ELIGIBLE
                : EligibilityStatus.INELIGIBLE;
    }

    private boolean isUserValueMissing(UserProfile profile, String fieldName) {
        return switch (fieldName) {
            case "age" -> profile.getAge() == null;
            case "householdSize" -> profile.getHouseholdSize() == null;
            case "monthlyIncome" -> profile.getMonthlyIncome() == null;
            case "annualIncome" -> profile.getAnnualIncome() == null;
            case "middleIncomePercent" -> profile.getMiddleIncomePercent() == null;
            case "region" -> profile.getAddress() == null || profile.getAddress().isBlank();
            case "employed" -> profile.getEmployed() == null;
            case "student" -> profile.getStudent() == null;
            case "houseOwner" -> profile.getHouseOwner() == null;
            default -> false;
        };
    }

    private boolean checkCondition(UserProfile profile, BenefitCondition condition) {

        String fieldName = condition.getFieldName().trim();
        String operator = condition.getOperator().trim();
        String value = condition.getValue().trim();

        return switch (fieldName) {
            case "age" -> compareNumber(profile.getAge(), operator, value);
            case "householdSize" -> compareNumber(profile.getHouseholdSize(), operator, value);
            case "monthlyIncome" -> compareNumber(profile.getMonthlyIncome(), operator, value);
            case "annualIncome" -> compareNumber(profile.getAnnualIncome(), operator, value);
            case "middleIncomePercent" -> compareNumber(profile.getMiddleIncomePercent(), operator, value);
            case "region" -> compareAddress(profile.getAddress(), operator, value);
            case "employed" -> compareBoolean(profile.getEmployed(), operator, value);
            case "student" -> compareBoolean(profile.getStudent(), operator, value);
            case "houseOwner" -> compareBoolean(profile.getHouseOwner(), operator, value);
            default -> false;
        };
    }

    private String createUserValueLabel(UserProfile profile, BenefitCondition condition) {

        String fieldName = condition.getFieldName().trim();

        return switch (fieldName) {
            case "age" -> profile.getAge() == null ? "미입력" : profile.getAge() + "세";
            case "region" -> profile.getAddress() == null || profile.getAddress().isBlank()
                    ? "미입력" : profile.getAddress();
            case "householdSize" -> profile.getHouseholdSize() == null
                    ? "미입력" : profile.getHouseholdSize() + "명";
            case "monthlyIncome" -> profile.getMonthlyIncome() == null
                    ? "미입력" : profile.getMonthlyIncome() + "원";
            case "annualIncome" -> profile.getAnnualIncome() == null
                    ? "미입력" : profile.getAnnualIncome() + "원";
            case "middleIncomePercent" -> profile.getMiddleIncomePercent() == null
                    ? "미입력" : profile.getMiddleIncomePercent() + "%";
            case "employed" -> profile.getEmployed() == null
                    ? "미입력" : profile.getEmployed() ? "예" : "아니오";
            case "student" -> profile.getStudent() == null
                    ? "미입력" : profile.getStudent() ? "예" : "아니오";
            case "houseOwner" -> profile.getHouseOwner() == null
                    ? "미입력" : profile.getHouseOwner() ? "주택 보유" : "무주택";
            default -> "-";
        };
    }

    private String createMessage(
            BenefitCondition condition,
            EligibilityStatus status
    ) {
        String fieldName = condition.getFieldName().trim();

        if (status == EligibilityStatus.NEED_MORE_INFO) {
            return createMissingValueMessage(fieldName);
        }

        if (status == EligibilityStatus.ELIGIBLE) {
            return createSatisfiedMessage(fieldName);
        }

        return switch (fieldName) {
            case "age" -> "나이 조건을 충족하지 않습니다.";
            case "region" -> "거주지역 조건을 충족하지 않습니다.";
            case "householdSize" -> "가구원 수 조건을 충족하지 않습니다.";
            case "monthlyIncome" -> "월 소득 조건을 충족하지 않습니다.";
            case "annualIncome" -> "연 소득 조건을 충족하지 않습니다.";
            case "middleIncomePercent" -> "중위소득 기준을 충족하지 않습니다.";
            case "employed" -> "취업 여부 조건을 충족하지 않습니다.";
            case "student" -> "학생 여부 조건을 충족하지 않습니다.";
            case "houseOwner" -> "주택 보유 여부 조건을 충족하지 않습니다.";
            default -> "조건을 충족하지 않습니다.";
        };
    }

    private String createSatisfiedMessage(String fieldName) {
        return switch (fieldName) {
            case "age" -> "나이 조건을 충족했습니다.";
            case "region" -> "거주지역 조건을 충족했습니다.";
            case "householdSize" -> "가구원 수 조건을 충족했습니다.";
            case "monthlyIncome" -> "월 소득 조건을 충족했습니다.";
            case "annualIncome" -> "연 소득 조건을 충족했습니다.";
            case "middleIncomePercent" -> "중위소득 기준을 충족했습니다.";
            case "employed" -> "취업 여부 조건을 충족했습니다.";
            case "student" -> "학생 여부 조건을 충족했습니다.";
            case "houseOwner" -> "주택 보유 여부 조건을 충족했습니다.";
            default -> "조건을 충족했습니다.";
        };
    }

    private String createMissingValueMessage(String fieldName) {
        return switch (fieldName) {
            case "age" -> "나이 정보가 없어 확인이 필요합니다.";
            case "region" -> "거주지역 정보가 없어 확인이 필요합니다.";
            case "householdSize" -> "가구원 수 정보가 없어 확인이 필요합니다.";
            case "monthlyIncome" -> "월 소득 정보가 없어 확인이 필요합니다.";
            case "annualIncome" -> "연 소득 정보가 없어 확인이 필요합니다.";
            case "middleIncomePercent" -> "중위소득 정보가 없어 확인이 필요합니다.";
            case "employed" -> "취업 여부 정보가 없어 확인이 필요합니다.";
            case "student" -> "학생 여부 정보가 없어 확인이 필요합니다.";
            case "houseOwner" -> "주택 보유 여부 정보가 없어 확인이 필요합니다.";
            default -> "사용자 정보가 없어 확인이 필요합니다.";
        };
    }

    private boolean compareNumber(Integer userValue, String operator, String value) {
        int conditionValue = Integer.parseInt(value);

        return switch (operator) {
            case ">=" -> userValue >= conditionValue;
            case "<=" -> userValue <= conditionValue;
            case ">" -> userValue > conditionValue;
            case "<" -> userValue < conditionValue;
            case "=", "==" -> userValue.equals(conditionValue);
            case "!=" -> !userValue.equals(conditionValue);
            default -> false;
        };
    }

    private boolean compareAddress(String address, String operator, String conditionValue) {

        String normalizedAddress = normalizeRegion(address);
        String normalizedConditionValue = normalizeRegion(conditionValue);

        return switch (operator) {
            case "=", "==" -> normalizedAddress.contains(normalizedConditionValue);
            case "!=" -> !normalizedAddress.contains(normalizedConditionValue);
            default -> false;
        };
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
        Boolean conditionValue = Boolean.parseBoolean(value);

        return switch (operator) {
            case "=", "==" -> userValue.equals(conditionValue);
            case "!=" -> !userValue.equals(conditionValue);
            default -> false;
        };
    }
}

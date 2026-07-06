package com.taehyun.youthpolicyplatform.eligibility.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.dto.ConditionDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.benefit.util.ConditionDisplayUtil;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityConditionResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
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

        boolean eligible = true;

        for (BenefitCondition condition : benefit.getConditions()) {

            boolean passed = checkCondition(profile, condition);

            if (!passed && condition.getRequired()) {
                eligible = false;
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

                            passed,
                            createMessage(condition, profile, passed)
                    )
            );
        }

        return new EligibilityResultDto(
                benefit.getTitle(),
                eligible,
                conditionResults
        );
    }

    private boolean checkCondition(UserProfile profile, BenefitCondition condition) {

        String fieldName = condition.getFieldName().trim();
        String operator = condition.getOperator().trim();
        String value = condition.getValue().trim();

        return switch (fieldName) {
            case "age" -> compareNumber(profile.getAge(), operator, Integer.parseInt(value));
            case "householdSize" -> compareNumber(profile.getHouseholdSize(), operator, Integer.parseInt(value));
            case "monthlyIncome" -> compareNumber(profile.getMonthlyIncome(), operator, Integer.parseInt(value));
            case "annualIncome" -> compareNumber(profile.getAnnualIncome(), operator, Integer.parseInt(value));
            case "middleIncomePercent" -> compareNumber(profile.getMiddleIncomePercent(), operator, Integer.parseInt(value));
            case "region" -> compareAddress(profile.getAddress(), operator, value);
            case "employed" -> compareBoolean(profile.getEmployed(), operator, Boolean.parseBoolean(value));
            case "student" -> compareBoolean(profile.getStudent(), operator, Boolean.parseBoolean(value));
            case "houseOwner" -> compareBoolean(profile.getHouseOwner(), operator, Boolean.parseBoolean(value));
            default -> false;
        };
    }

    private String createUserValueLabel(UserProfile profile, BenefitCondition condition) {

        String fieldName = condition.getFieldName().trim();

        return switch (fieldName) {
            case "age" -> profile.getAge() + "세";
            case "region" -> profile.getAddress();
            case "householdSize" -> profile.getHouseholdSize() + "명";
            case "monthlyIncome" -> profile.getMonthlyIncome() + "만원";
            case "annualIncome" -> profile.getAnnualIncome() + "만원";
            case "middleIncomePercent" -> profile.getMiddleIncomePercent() + "%";
            case "employed" -> profile.getEmployed() ? "예" : "아니오";
            case "student" -> profile.getStudent() ? "예" : "아니오";
            case "houseOwner" -> profile.getHouseOwner() ? "주택 보유" : "무주택";
            default -> "-";
        };
    }

    private String createMessage(
            BenefitCondition condition,
            UserProfile profile,
            boolean passed
    ) {
        String fieldName = condition.getFieldName().trim();

        if (passed) {
            return "조건을 충족했습니다.";
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

    private boolean compareNumber(Integer userValue, String operator, Integer conditionValue) {

        if (userValue == null) {
            return false;
        }

        return switch (operator) {
            case ">=" -> userValue >= conditionValue;
            case "<=" -> userValue <= conditionValue;
            case "==" -> userValue.equals(conditionValue);
            case "!=" -> !userValue.equals(conditionValue);
            default -> false;
        };
    }

    private boolean compareAddress(String address, String operator, String conditionValue) {

        if (address == null || address.isBlank()) {
            return false;
        }

        String normalizedAddress = normalizeRegion(address);
        String normalizedConditionValue = normalizeRegion(conditionValue);

        return switch (operator) {
            case "==" -> normalizedAddress.contains(normalizedConditionValue);
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

    private boolean compareBoolean(Boolean userValue, String operator, Boolean conditionValue) {

        if (userValue == null) {
            return false;
        }

        return switch (operator) {
            case "==" -> userValue.equals(conditionValue);
            case "!=" -> !userValue.equals(conditionValue);
            default -> false;
        };
    }
}
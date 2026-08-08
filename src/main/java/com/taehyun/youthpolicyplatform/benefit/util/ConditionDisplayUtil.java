package com.taehyun.youthpolicyplatform.benefit.util;

import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.dto.ConditionDisplayDto;

public class ConditionDisplayUtil {

    public static ConditionDisplayDto convert(BenefitCondition condition) {

        String fieldName = trimToEmpty(condition.getFieldName());
        String operator = trimToEmpty(condition.getOperator());
        String value = trimToEmpty(condition.getValue());

        String fieldLabel = convertField(fieldName);
        String operatorLabel = convertOperator(fieldName, operator);
        String valueLabel = convertValue(fieldName, value);

        return new ConditionDisplayDto(
                fieldLabel,
                operatorLabel,
                valueLabel
        );
    }

    private static String convertField(String fieldName) {

        return switch (fieldName) {
            case "birthDate" -> "생년월일";
            case "age" -> "나이";
            case "region" -> "거주지역";
            case "address" -> "주소";
            case "householdSize" -> "가구원 수";
            case "monthlyIncome", "monthlyEarnedIncome" -> "월 근로·사업소득";
            case "annualIncome", "annualPersonalIncome" -> "개인 연소득";
            case "householdMonthlyIncome" -> "가구 월소득";
            case "middleIncomePercent" -> "중위소득";
            case "employed" -> "취업 여부";
            case "employmentStatus" -> "취업 상태";
            case "student" -> "학생 여부";
            case "educationStatus" -> "학적 상태";
            case "houseOwner" -> "주택 보유 여부";
            case "housingOwnershipStatus" -> "주택 소유 상태";
            case "graduationDate" -> "졸업 시점";
            case "graduationMonths" -> "졸업 후 경과 기간";
            case "employmentType" -> "고용 형태";
            case "smeEmployee" -> "중소기업 재직 여부";
            case "jobSeekingStatus" -> "구직 상태";
            default -> fieldName;
        };
    }

    private static String convertOperator(String fieldName, String operator) {

        if (fieldName.equals("region") || fieldName.equals("address")) {
            return switch (operator) {
                case "IN" -> "중 하나";
                case "NOT_IN" -> "제외";
                default -> "";
            };
        }

        return switch (operator) {
            case ">=" -> "이상";
            case "<=" -> "이하";
            case "==" -> "같음";
            case "!=" -> "다름";
            default -> operator;
        };
    }

    private static String convertValue(String fieldName, String value) {

        return switch (fieldName) {
            case "age" -> value + "세";
            case "monthlyIncome", "monthlyEarnedIncome",
                    "annualIncome", "annualPersonalIncome",
                    "householdMonthlyIncome" -> value + "원";
            case "middleIncomePercent" -> value + "%";
            case "graduationMonths" -> value + "개월";
            case "smeEmployee" -> value.equals("true") ? "예" : "아니요";
            case "employmentType" -> convertEnumValues(value, true);
            case "jobSeekingStatus" -> convertEnumValues(value, false);
            case "region", "address" -> value + " 거주";
            case "employed", "student" -> value.equals("true") ? "예" : "아니오";
            case "houseOwner" -> value.equals("true") ? "주택 보유" : "무주택";
            default -> value;
        };
    }

    private static String convertEnumValues(String value, boolean employmentType) {
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .map(item -> employmentType ? employmentTypeLabel(item) : jobSeekingStatusLabel(item))
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private static String employmentTypeLabel(String value) {
        return switch (value) {
            case "FULL_TIME" -> "정규직";
            case "PART_TIME" -> "시간제·파트타임";
            case "CONTRACT" -> "계약직";
            case "DAILY" -> "일용직";
            case "PLATFORM" -> "플랫폼 노동";
            case "OTHER" -> "기타";
            default -> value;
        };
    }

    private static String jobSeekingStatusLabel(String value) {
        return switch (value) {
            case "REGISTERED" -> "구직 등록 중";
            case "SEEKING_NOT_REGISTERED" -> "구직 중이지만 등록하지 않음";
            case "NOT_SEEKING" -> "현재 구직 중이 아님";
            default -> value;
        };
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}

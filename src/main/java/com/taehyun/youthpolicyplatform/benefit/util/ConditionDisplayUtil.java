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
            case "region", "address" -> value + " 거주";
            case "employed", "student" -> value.equals("true") ? "예" : "아니오";
            case "houseOwner" -> value.equals("true") ? "주택 보유" : "무주택";
            default -> value;
        };
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}

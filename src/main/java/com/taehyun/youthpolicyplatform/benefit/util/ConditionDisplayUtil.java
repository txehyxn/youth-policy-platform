package com.taehyun.youthpolicyplatform.benefit.util;

import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.dto.ConditionDisplayDto;

public class ConditionDisplayUtil {

    public static ConditionDisplayDto convert(BenefitCondition condition) {

        String fieldName = condition.getFieldName().trim();
        String operator = condition.getOperator().trim();
        String value = condition.getValue().trim();

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
            case "age" -> "나이";
            case "region" -> "거주지역";
            case "address" -> "주소";
            case "householdSize" -> "가구원 수";
            case "monthlyIncome" -> "월 소득";
            case "annualIncome" -> "연 소득";
            case "middleIncomePercent" -> "중위소득";
            case "employed" -> "취업 여부";
            case "student" -> "학생 여부";
            case "houseOwner" -> "주택 보유 여부";
            default -> fieldName;
        };
    }

    private static String convertOperator(String fieldName, String operator) {

        if (fieldName.equals("region") || fieldName.equals("address")) {
            return "";
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
            case "monthlyIncome", "annualIncome" -> value + "원";
            case "middleIncomePercent" -> value + "%";
            case "region", "address" -> value + " 거주";
            case "employed", "student" -> value.equals("true") ? "예" : "아니오";
            case "houseOwner" -> value.equals("true") ? "주택 보유" : "무주택";
            default -> value;
        };
    }
}
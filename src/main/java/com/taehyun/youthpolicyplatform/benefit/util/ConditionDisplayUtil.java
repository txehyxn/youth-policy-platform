package com.taehyun.youthpolicyplatform.benefit.util;

import com.taehyun.youthpolicyplatform.benefit.dto.ConditionDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;

public class ConditionDisplayUtil {

    public static ConditionDisplayDto convert(BenefitCondition condition) {

        String fieldLabel = convertField(condition.getFieldName());
        String operatorLabel = convertOperator(condition.getOperator());
        String valueLabel = convertValue(
                condition.getFieldName(),
                condition.getValue()
        );

        return new ConditionDisplayDto(
                fieldLabel,
                operatorLabel,
                valueLabel
        );
    }

    private static String convertField(String fieldName) {

        return switch (fieldName) {
            case "age" -> "나이";
            case "middle_income_percent" -> "중위소득";
            case "house_owner" -> "주택 소유 여부";
            default -> fieldName;
        };
    }

    private static String convertOperator(String operator) {

        return switch (operator) {
            case ">=" -> "이상";
            case "<=" -> "이하";
            case "=" -> "같음";
            default -> operator;
        };
    }

    private static String convertValue(String fieldName, String value) {

        return switch (fieldName) {
            case "age" -> value + "세";
            case "middle_income_percent" -> value + "%";
            case "house_owner" ->
                    value.equals("false") ? "무주택" : "주택 보유";
            default -> value;
        };
    }
}
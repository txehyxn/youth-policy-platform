package com.taehyun.youthpolicyplatform.user.util;

public class IncomeCalculator {

    // 월소득과 가구원 수를 받아서, 기준중위소득 대비 몇 %인지 계산하는 함수
    public static int calculateMiddleIncomePercent(
            Integer monthlyIncome,
            Integer householdSize
    ) {

        if (monthlyIncome == null || householdSize == null || householdSize <= 0) {
            return 0;
        }

        int medianIncome = MedianIncomeTable.getMedianIncome(householdSize);

        double percent = (monthlyIncome * 100.0) / medianIncome;

        // 소수점은 반올림해서 정수(%)로 돌려준다
        return (int) Math.round(percent);
    }
}
package com.taehyun.youthpolicyplatform.user.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeCalculatorTest {

    @Test
    void returnsNullWhenRequiredCalculationInputIsMissingOrInvalid() {
        assertThat(IncomeCalculator.calculateMiddleIncomePercent(null, 1)).isNull();
        assertThat(IncomeCalculator.calculateMiddleIncomePercent(1_000_000L, null)).isNull();
        assertThat(IncomeCalculator.calculateMiddleIncomePercent(1_000_000L, 0)).isNull();
    }

    @Test
    void distinguishesActualZeroIncomeFromMissingIncome() {
        assertThat(IncomeCalculator.calculateMiddleIncomePercent(0L, 1)).isZero();
    }

    @Test
    void keepsNormalMiddleIncomeCalculation() {
        int monthlyIncome = 2_000_000;
        int expected = (int) Math.round(
                monthlyIncome * 100.0 / MedianIncomeTable.getMedianIncome(1)
        );

        assertThat(IncomeCalculator.calculateMiddleIncomePercent(monthlyIncome, 1))
                .isEqualTo(expected);
    }
}

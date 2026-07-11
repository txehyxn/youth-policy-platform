package com.taehyun.youthpolicyplatform.user.util;

import java.util.Map;

// 보건복지부 고시 2026년 기준중위소득 (월 기준, 100% 금액)
public class MedianIncomeTable {

    // 가구원 수(1~7인)별 월 기준중위소득
    private static final Map<Integer, Integer> TABLE = Map.of(
            1, 2564238,
            2, 4199292,
            3, 5359036,
            4, 6494738,
            5, 7556719,
            6, 8555952,
            7, 9515150
    );

    // 7인 초과 시, 1인 늘어날 때마다 추가되는 금액
    private static final int EXTRA_PER_PERSON = 959198;

    // 가구원 수를 넣으면 그 가구의 월 기준중위소득(100%)을 돌려주는 함수
    public static int getMedianIncome(int householdSize) {

        if (householdSize <= 7) {
            return TABLE.get(householdSize);
        }

        // 8인 이상이면: 7인 기준금액 + (초과 인원수 × 추가금액)
        int extraPeople = householdSize - 7;
        return TABLE.get(7) + (extraPeople * EXTRA_PER_PERSON);
    }
}
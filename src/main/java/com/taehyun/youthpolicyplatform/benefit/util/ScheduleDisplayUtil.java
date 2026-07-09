package com.taehyun.youthpolicyplatform.benefit.util;

import com.taehyun.youthpolicyplatform.benefit.domain.BenefitSchedule;
import com.taehyun.youthpolicyplatform.benefit.dto.ScheduleDisplayDto;

import java.time.LocalDate;

public class ScheduleDisplayUtil {

    // BenefitSchedule 하나를 받아서, 화면에 보여줄 형태(ScheduleDisplayDto)로 바꿔주는 함수
    public static ScheduleDisplayDto convert(BenefitSchedule schedule) {

        String statusLabel = calculateStatus(
                schedule.getStartDate(),
                schedule.getEndDate()
        );

        return new ScheduleDisplayDto(
                schedule.getTitle(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getDescription(),
                statusLabel
        );
    }

    // 오늘 날짜를 기준으로 "신청 예정" / "신청중" / "신청 마감"을 계산하는 함수
    private static String calculateStatus(LocalDate startDate, LocalDate endDate) {

        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return "신청 예정";
        }

        if (today.isAfter(endDate)) {
            return "신청 마감";
        }

        return "신청중";
    }
}
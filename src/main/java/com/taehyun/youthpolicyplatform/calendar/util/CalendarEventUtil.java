package com.taehyun.youthpolicyplatform.calendar.util;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitSchedule;
import com.taehyun.youthpolicyplatform.calendar.dto.CalendarEventDto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CalendarEventUtil {

    // 마감까지 이 날짜(3일) 이내로 남으면 "마감임박"으로 취급한다
    private static final long URGENT_THRESHOLD_DAYS = 3;

    // 일정 하나(BenefitSchedule)를 받아서 달력용 데이터(CalendarEventDto)로 바꿔주는 함수
    // 상시 신청(alwaysOpen)이면 날짜가 없어서 달력에 점으로 찍을 수 없으므로 null을 돌려준다
    public static CalendarEventDto convert(BenefitSchedule schedule) {

        // 상시 신청은 날짜 기반 달력에 표시할 수 없으므로 건너뛴다
        if (Boolean.TRUE.equals(schedule.getAlwaysOpen())) {
            return null;
        }

        Benefit benefit = schedule.getBenefit();

        LocalDate startDate = schedule.getStartDate();
        LocalDate endDate = schedule.getEndDate();

        String statusLabel = calculateStatusLabel(startDate, endDate);
        String colorClass = calculateColorClass(startDate, endDate);
        String dDayLabel = calculateDDayLabel(startDate, endDate);

        return new CalendarEventDto(
                benefit.getTitle(),
                startDate.toString(),
                endDate.toString(),
                "/benefits/" + benefit.getId(),
                benefit.getCategory().getName(),
                statusLabel,
                colorClass,
                dDayLabel
        );
    }

    // 오늘 날짜를 기준으로 상태 이름(사람이 읽는 글자)을 정하는 함수
    private static String calculateStatusLabel(LocalDate startDate, LocalDate endDate) {

        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return "신청 예정";
        }

        if (today.isAfter(endDate)) {
            return "신청 마감";
        }

        long daysUntilEnd = ChronoUnit.DAYS.between(today, endDate);

        if (daysUntilEnd <= URGENT_THRESHOLD_DAYS) {
            return "마감 임박";
        }

        return "신청중";
    }

    // 화면 색칠할 때 쓸 영어 이름표를 정하는 함수
    private static String calculateColorClass(LocalDate startDate, LocalDate endDate) {

        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return "upcoming";
        }

        if (today.isAfter(endDate)) {
            return "closed";
        }

        long daysUntilEnd = ChronoUnit.DAYS.between(today, endDate);

        if (daysUntilEnd <= URGENT_THRESHOLD_DAYS) {
            return "urgent";
        }

        return "ongoing";
    }

    // "마감 D-3", "시작 D-5" 처럼 며칠 남았는지 글자로 만드는 함수
    private static String calculateDDayLabel(LocalDate startDate, LocalDate endDate) {

        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            long daysUntilStart = ChronoUnit.DAYS.between(today, startDate);
            return "시작 D-" + daysUntilStart;
        }

        if (today.isAfter(endDate)) {
            return "마감됨";
        }

        long daysUntilEnd = ChronoUnit.DAYS.between(today, endDate);

        if (daysUntilEnd == 0) {
            return "오늘 마감";
        }

        return "마감 D-" + daysUntilEnd;
    }
}
package com.taehyun.youthpolicyplatform.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// FullCalendar(달력 라이브러리)가 이해하는 형식에 맞춘 일정 데이터
@Getter
@AllArgsConstructor
public class CalendarEventDto {

    // 달력에 표시될 제목 (예: "청년미래적금")
    private String title;

    // 시작일 (yyyy-MM-dd 형식의 문자열)
    private String start;

    // 종료일 (yyyy-MM-dd 형식의 문자열)
    private String end;

    // 클릭했을 때 이동할 정책 상세 페이지 주소
    private String url;

    // 정책 카테고리명 (필터에서 사용)
    private String categoryName;

    // 상태: "예정" / "신청중" / "마감임박" / "마감"
    private String statusLabel;

    // 색깔을 정할 때 쓸 이름표: "upcoming" / "ongoing" / "urgent" / "closed"
    private String colorClass;

    // 화면에 보여줄 D-day 글자 (예: "마감 D-3", "시작 D-5", "마감됨")
    private String dDayLabel;
}
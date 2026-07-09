package com.taehyun.youthpolicyplatform.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ScheduleDisplayDto {

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    // 신청 상태: 예정 / 신청중 / 마감
    private String statusLabel;
}
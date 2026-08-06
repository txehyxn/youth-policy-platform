package com.taehyun.youthpolicyplatform.user.dto;

import com.taehyun.youthpolicyplatform.user.domain.EducationStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentStatus;
import com.taehyun.youthpolicyplatform.user.domain.HousingOwnershipStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class UserProfileRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    private String address;

    private String regionCode;

    private Integer householdSize;

    private Long monthlyEarnedIncome;

    private EmploymentStatus employmentStatus;

    private EducationStatus educationStatus;

    private HousingOwnershipStatus housingOwnershipStatus;
}

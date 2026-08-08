package com.taehyun.youthpolicyplatform.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.taehyun.youthpolicyplatform.user.domain.EducationStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentStatus;
import com.taehyun.youthpolicyplatform.user.domain.HousingOwnershipStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentType;
import com.taehyun.youthpolicyplatform.user.domain.JobSeekingStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
public class UserProfilePatchRequest {

    @JsonIgnore
    private final Set<String> presentFields = new HashSet<>();

    private LocalDate birthDate;
    private String address;
    private String regionCode;
    private Integer householdSize;
    private Long monthlyEarnedIncome;
    private EmploymentStatus employmentStatus;
    private EducationStatus educationStatus;
    private HousingOwnershipStatus housingOwnershipStatus;
    private LocalDate graduationDate;
    private EmploymentType employmentType;
    private Boolean smeEmployee;
    private JobSeekingStatus jobSeekingStatus;

    @JsonSetter("birthDate")
    public void setBirthDate(LocalDate birthDate) {
        presentFields.add("birthDate");
        this.birthDate = birthDate;
    }

    @JsonSetter("address")
    public void setAddress(String address) {
        presentFields.add("address");
        this.address = address;
    }

    @JsonSetter("regionCode")
    public void setRegionCode(String regionCode) {
        presentFields.add("regionCode");
        this.regionCode = regionCode;
    }

    @JsonSetter("householdSize")
    public void setHouseholdSize(Integer householdSize) {
        presentFields.add("householdSize");
        this.householdSize = householdSize;
    }

    @JsonSetter("monthlyEarnedIncome")
    public void setMonthlyEarnedIncome(Long monthlyEarnedIncome) {
        presentFields.add("monthlyEarnedIncome");
        this.monthlyEarnedIncome = monthlyEarnedIncome;
    }

    @JsonSetter("employmentStatus")
    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        presentFields.add("employmentStatus");
        this.employmentStatus = employmentStatus;
    }

    @JsonSetter("educationStatus")
    public void setEducationStatus(EducationStatus educationStatus) {
        presentFields.add("educationStatus");
        this.educationStatus = educationStatus;
    }

    @JsonSetter("housingOwnershipStatus")
    public void setHousingOwnershipStatus(
            HousingOwnershipStatus housingOwnershipStatus
    ) {
        presentFields.add("housingOwnershipStatus");
        this.housingOwnershipStatus = housingOwnershipStatus;
    }

    @JsonSetter("graduationDate")
    public void setGraduationDate(LocalDate graduationDate) {
        presentFields.add("graduationDate");
        this.graduationDate = graduationDate;
    }

    @JsonSetter("employmentType")
    public void setEmploymentType(EmploymentType employmentType) {
        presentFields.add("employmentType");
        this.employmentType = employmentType;
    }

    @JsonSetter("smeEmployee")
    public void setSmeEmployee(Boolean smeEmployee) {
        presentFields.add("smeEmployee");
        this.smeEmployee = smeEmployee;
    }

    @JsonSetter("jobSeekingStatus")
    public void setJobSeekingStatus(JobSeekingStatus jobSeekingStatus) {
        presentFields.add("jobSeekingStatus");
        this.jobSeekingStatus = jobSeekingStatus;
    }

    public boolean hasField(String fieldName) {
        return presentFields.contains(fieldName);
    }
}

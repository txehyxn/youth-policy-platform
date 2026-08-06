package com.taehyun.youthpolicyplatform.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

// 정책 판별에 공통적으로 사용되는 사용자 기본 조건 정보
@Getter
@NoArgsConstructor
@Entity
public class UserProfile {

    // 프로필 고유 번호(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 나이
    private Integer age;

    // 주소
    private String address;

    // 가구원 수
    private Integer householdSize;

    // 월소득
    private Integer monthlyIncome;

    // 연소득
    private Integer annualIncome;

    // 중위소득 비율
    private Integer middleIncomePercent;

    // 취업 여부
    private Boolean employed;

    // 학생 여부
    private Boolean student;

    // 자가 보유 여부
    private Boolean houseOwner;

    // 실시간 정책 판정을 위한 1차 핵심 프로필 필드. null은 아직 입력하지 않은 상태다.
    private LocalDate birthDate;

    private String regionCode;

    private Long monthlyEarnedIncome;

    private Long annualPersonalIncome;

    private Long householdMonthlyIncome;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus;

    @Enumerated(EnumType.STRING)
    private EducationStatus educationStatus;

    @Enumerated(EnumType.STRING)
    private HousingOwnershipStatus housingOwnershipStatus;

    // 해당 프로필을 작성한 회원
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 사용자 프로필 생성자
    public UserProfile(
            Integer age,
            String address,
            Integer householdSize,
            Integer monthlyIncome,
            Integer annualIncome,
            Integer middleIncomePercent,
            Boolean employed,
            Boolean student,
            Boolean houseOwner,
            User user
    ) {
        this.age = age;
        this.address = address;
        this.householdSize = householdSize;
        this.monthlyIncome = monthlyIncome;
        this.annualIncome = annualIncome;
        this.middleIncomePercent = middleIncomePercent;
        this.employed = employed;
        this.student = student;
        this.houseOwner = houseOwner;
        this.user = user;
    }

    public UserProfile(
            LocalDate birthDate,
            String regionCode,
            Integer householdSize,
            Long monthlyEarnedIncome,
            Long annualPersonalIncome,
            Long householdMonthlyIncome,
            Integer middleIncomePercent,
            EmploymentStatus employmentStatus,
            EducationStatus educationStatus,
            HousingOwnershipStatus housingOwnershipStatus,
            User user
    ) {
        this.birthDate = birthDate;
        this.regionCode = regionCode;
        this.householdSize = householdSize;
        this.monthlyEarnedIncome = monthlyEarnedIncome;
        this.annualPersonalIncome = annualPersonalIncome;
        this.householdMonthlyIncome = householdMonthlyIncome;
        this.middleIncomePercent = middleIncomePercent;
        this.employmentStatus = employmentStatus;
        this.educationStatus = educationStatus;
        this.housingOwnershipStatus = housingOwnershipStatus;
        this.user = user;
    }

    // 사용자 프로필 수정
    public void update(
            Integer age,
            String address,
            Integer householdSize,
            Integer monthlyIncome,
            Integer annualIncome,
            Integer middleIncomePercent,
            Boolean employed,
            Boolean student,
            Boolean houseOwner
    ) {
        this.age = age;
        this.address = address;
        this.householdSize = householdSize;
        this.monthlyIncome = monthlyIncome;
        this.annualIncome = annualIncome;
        this.middleIncomePercent = middleIncomePercent;
        this.employed = employed;
        this.student = student;
        this.houseOwner = houseOwner;
    }

    public Integer getEligibilityAge() {
        if (birthDate != null) {
            return Period.between(birthDate, LocalDate.now()).getYears();
        }
        return age;
    }

    public void updateProfileInputs(
            LocalDate birthDate,
            String address,
            String regionCode,
            Integer householdSize,
            Long monthlyEarnedIncome,
            Integer middleIncomePercent,
            EmploymentStatus employmentStatus,
            EducationStatus educationStatus,
            HousingOwnershipStatus housingOwnershipStatus
    ) {
        this.birthDate = birthDate;
        this.address = address;
        this.regionCode = regionCode;
        this.householdSize = householdSize;
        this.monthlyEarnedIncome = monthlyEarnedIncome;
        this.middleIncomePercent = middleIncomePercent;
        this.employmentStatus = employmentStatus;
        this.educationStatus = educationStatus;
        this.housingOwnershipStatus = housingOwnershipStatus;

        // 새 프로필 화면에서 저장한 뒤에는 신규 필드가 우선 판정값이 된다.
        this.monthlyIncome = null;
        this.employed = null;
        this.student = null;
        this.houseOwner = null;
    }

    public void updateBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public void updateHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    public void updateMonthlyEarnedIncome(Long monthlyEarnedIncome) {
        this.monthlyEarnedIncome = monthlyEarnedIncome;
        this.monthlyIncome = null;
    }

    public void updateEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
        this.employed = null;
    }

    public void updateEducationStatus(EducationStatus educationStatus) {
        this.educationStatus = educationStatus;
        this.student = null;
    }

    public void updateHousingOwnershipStatus(
            HousingOwnershipStatus housingOwnershipStatus
    ) {
        this.housingOwnershipStatus = housingOwnershipStatus;
        this.houseOwner = null;
    }

    public void updateMiddleIncomePercent(Integer middleIncomePercent) {
        this.middleIncomePercent = middleIncomePercent;
    }

    public Long getEligibilityMonthlyEarnedIncome() {
        if (monthlyEarnedIncome != null) {
            return monthlyEarnedIncome;
        }
        return monthlyIncome == null ? null : monthlyIncome.longValue();
    }

    public Long getEligibilityAnnualPersonalIncome() {
        if (annualPersonalIncome != null) {
            return annualPersonalIncome;
        }
        return annualIncome == null ? null : annualIncome.longValue();
    }

    public EmploymentStatus getEligibilityEmploymentStatus() {
        if (employmentStatus != null) {
            return employmentStatus;
        }
        if (employed == null) {
            return null;
        }
        return employed ? EmploymentStatus.EMPLOYED : EmploymentStatus.UNEMPLOYED;
    }

    public Boolean getEligibilityEmployed() {
        if (employmentStatus != null) {
            return employmentStatus == EmploymentStatus.EMPLOYED
                    || employmentStatus == EmploymentStatus.SELF_EMPLOYED;
        }
        return employed;
    }

    public EducationStatus getEligibilityEducationStatus() {
        if (educationStatus != null) {
            return educationStatus;
        }
        if (Boolean.TRUE.equals(student)) {
            return EducationStatus.UNIVERSITY_ENROLLED;
        }
        return null;
    }

    public Boolean getEligibilityStudent() {
        if (educationStatus != null) {
            return educationStatus == EducationStatus.HIGH_SCHOOL_STUDENT
                    || educationStatus == EducationStatus.UNIVERSITY_ENROLLED
                    || educationStatus == EducationStatus.LEAVE_OF_ABSENCE
                    || educationStatus == EducationStatus.EXPECTED_GRADUATION;
        }
        return student;
    }

    public HousingOwnershipStatus getEligibilityHousingOwnershipStatus() {
        if (housingOwnershipStatus != null) {
            return housingOwnershipStatus;
        }
        if (houseOwner == null) {
            return null;
        }
        return houseOwner
                ? HousingOwnershipStatus.APPLICANT_OWNS
                : HousingOwnershipStatus.NO_HOME;
    }

    public Boolean getEligibilityHouseOwner() {
        if (housingOwnershipStatus != null) {
            return housingOwnershipStatus != HousingOwnershipStatus.NO_HOME;
        }
        return houseOwner;
    }
}

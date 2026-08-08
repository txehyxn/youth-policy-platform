package com.taehyun.youthpolicyplatform.eligibility.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityStatus;
import com.taehyun.youthpolicyplatform.user.domain.EducationStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentType;
import com.taehyun.youthpolicyplatform.user.domain.HousingOwnershipStatus;
import com.taehyun.youthpolicyplatform.user.domain.JobSeekingStatus;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EligibilityServiceTest {

    private BenefitRepository benefitRepository;
    private UserProfileRepository userProfileRepository;
    private EligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        benefitRepository = mock(BenefitRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        eligibilityService = new EligibilityService(benefitRepository, userProfileRepository);
    }

    @Test
    void returnsEligibleWhenAllRequiredConditionsAreSatisfied() {
        Benefit benefit = benefitWithConditions(
                condition("age", ">=", "19"),
                condition("age", "<=", "34")
        );
        UserProfile profile = completeProfile();
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.isEligible()).isTrue();
        assertThat(result.getConditionResults())
                .allMatch(condition -> condition.getStatus() == EligibilityStatus.ELIGIBLE);
    }

    @Test
    void returnsIneligibleWhenOneRequiredConditionIsNotSatisfied() {
        Benefit benefit = benefitWithConditions(
                condition("middleIncomePercent", "<=", "60")
        );
        UserProfile profile = completeProfile();
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.INELIGIBLE);
        assertThat(result.getConditionResults().getFirst().getMessage())
                .isEqualTo("중위소득 기준을 충족하지 않습니다.");
    }

    @Test
    void returnsNeedMoreInfoWhenRequiredUserValueIsMissing() {
        Benefit benefit = benefitWithConditions(
                condition("employed", "==", "true")
        );
        UserProfile profile = profileWithEmployed(null);
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.NEED_MORE_INFO);
        assertThat(result.isEligible()).isFalse();
        assertThat(result.getConditionResults().getFirst().getStatus())
                .isEqualTo(EligibilityStatus.NEED_MORE_INFO);
        assertThat(result.getConditionResults().getFirst().getMessage())
                .isEqualTo("취업 여부 정보가 없어 확인이 필요합니다.");
    }

    @Test
    void returnsIneligibleWhenAnyOfMultipleRequiredConditionsFails() {
        Benefit benefit = benefitWithConditions(
                condition("age", ">=", "19"),
                condition("region", "==", "서울"),
                condition("middleIncomePercent", "<=", "60")
        );
        UserProfile profile = completeProfile();
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.INELIGIBLE);
        assertThat(result.getConditionResults())
                .extracting("status")
                .containsExactly(
                        EligibilityStatus.ELIGIBLE,
                        EligibilityStatus.ELIGIBLE,
                        EligibilityStatus.INELIGIBLE
                );
    }

    @Test
    void returnsEligibleWhenMultipleRequiredConditionsAreAllSatisfied() {
        Benefit benefit = benefitWithConditions(
                condition("age", ">=", "19"),
                condition("region", "==", "서울"),
                condition("employed", "==", "true"),
                condition("student", "==", "false"),
                condition("houseOwner", "==", "false")
        );
        UserProfile profile = completeProfile();
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.getConditionResults()).hasSize(5)
                .allMatch(condition -> condition.isPassed());
    }

    @Test
    void returnsEligibleWhenRegionIsInList() {
        Benefit benefit = benefitWithConditions(
                condition("region", "IN", "서울,경기,인천")
        );
        UserProfile profile = profileWithAddress("경기도 수원시");
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.getConditionResults().getFirst().isPassed()).isTrue();
    }

    @Test
    void returnsIneligibleWhenRegionIsNotInList() {
        Benefit benefit = benefitWithConditions(
                condition("region", "IN", "서울,부산")
        );
        UserProfile profile = profileWithAddress("경기도 수원시");
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.INELIGIBLE);
        assertThat(result.getConditionResults().getFirst().isPassed()).isFalse();
    }

    @Test
    void returnsEligibleWhenRegionIsNotInExcludedList() {
        Benefit benefit = benefitWithConditions(
                condition("region", "NOT_IN", "서울,부산")
        );
        UserProfile profile = profileWithAddress("경기도 수원시");
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.getConditionResults().getFirst().isPassed()).isTrue();
    }

    @Test
    void returnsIneligibleWhenRegionIsInExcludedList() {
        Benefit benefit = benefitWithConditions(
                condition("region", "NOT_IN", "서울,경기")
        );
        UserProfile profile = profileWithAddress("경기도 수원시");
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.INELIGIBLE);
        assertThat(result.getConditionResults().getFirst().isPassed()).isFalse();
    }

    @Test
    void trimsWhitespaceInRegionList() {
        Benefit benefit = benefitWithConditions(
                condition("region", "IN", "서울, 경기, 인천")
        );
        UserProfile profile = profileWithAddress("인천광역시 남동구");
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void returnsNeedMoreInfoWhenRegionIsMissingForRequiredInCondition() {
        Benefit benefit = benefitWithConditions(
                condition("region", "IN", "서울,경기,인천")
        );
        UserProfile profile = profileWithAddress(null);
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.NEED_MORE_INFO);
        assertThat(result.getConditionResults().getFirst().getStatus())
                .isEqualTo(EligibilityStatus.NEED_MORE_INFO);
    }

    @Test
    void returnsIneligibleWithoutExceptionForInvalidRegionConditions() {
        String[][] invalidConditions = {
                {"IN", ""},
                {"IN", ",,,"},
                {"NOT_IN", "   "},
                {"UNSUPPORTED", "서울"}
        };

        for (String[] invalidCondition : invalidConditions) {
            Benefit benefit = benefitWithConditions(
                    condition("region", invalidCondition[0], invalidCondition[1])
            );
            UserProfile profile = profileWithAddress("경기도 수원시");
            prepareRepositories(benefit, profile);

            EligibilityResultDto result = eligibilityService.check(1L, 1L);

            assertThat(result.getStatus()).isEqualTo(EligibilityStatus.INELIGIBLE);
        }
    }

    @Test
    void checksAllBenefitsWithAlreadyLoadedProfileWithoutRepositoryLookup() {
        Benefit eligible = benefitWithConditions(condition("age", ">=", "19"));
        Benefit needMoreInfo = benefitWithConditions(condition("employed", "==", "true"));
        Benefit ineligible = benefitWithConditions(condition("age", "<=", "18"));
        ReflectionTestUtils.setField(eligible, "id", 1L);
        ReflectionTestUtils.setField(needMoreInfo, "id", 2L);
        ReflectionTestUtils.setField(ineligible, "id", 3L);
        UserProfile profile = profileWithEmployed(null);

        Map<Long, EligibilityResultDto> results = eligibilityService.checkAll(
                List.of(eligible, needMoreInfo, ineligible),
                profile
        );

        assertThat(results).extractingByKeys(1L, 2L, 3L)
                .extracting(EligibilityResultDto::getStatus)
                .containsExactly(
                        EligibilityStatus.ELIGIBLE,
                        EligibilityStatus.NEED_MORE_INFO,
                        EligibilityStatus.INELIGIBLE
                );
        verifyNoInteractions(benefitRepository, userProfileRepository);
    }

    @Test
    void usesBirthDateBasedAgeBeforeLegacyAge() {
        Benefit benefit = benefitWithConditions(condition("age", "==", "26"));
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                EmploymentStatus.EMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        ReflectionTestUtils.setField(profile, "age", 40);
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void returnsNeedMoreInfoWhenBirthDateAndLegacyAgeAreMissing() {
        Benefit benefit = benefitWithConditions(condition("age", ">=", "19"));
        UserProfile profile = newProfile(
                null, 0L,
                EmploymentStatus.EMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.NEED_MORE_INFO);
    }

    @Test
    void returnsNeedMoreInfoWhenEmploymentStatusIsMissing() {
        Benefit benefit = benefitWithConditions(
                condition("employmentStatus", "==", "EMPLOYED")
        );
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                null,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.NEED_MORE_INFO);
    }

    @Test
    void comparesEmploymentStatusEnumWithListOperator() {
        Benefit benefit = benefitWithConditions(
                condition("employmentStatus", "IN", "UNEMPLOYED, EMPLOYED")
        );
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                EmploymentStatus.EMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void comparesHousingOwnershipStatusEnum() {
        Benefit benefit = benefitWithConditions(
                condition("housingOwnershipStatus", "==", "NO_HOME")
        );
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                EmploymentStatus.EMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void comparesEducationStatusEnum() {
        Benefit benefit = benefitWithConditions(
                condition("educationStatus", "IN", "UNIVERSITY_ENROLLED, EXPECTED_GRADUATION")
        );
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(22), 0L,
                EmploymentStatus.NOT_ECONOMICALLY_ACTIVE,
                EducationStatus.UNIVERSITY_ENROLLED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void distinguishesZeroIncomeFromMissingIncome() {
        Benefit benefit = benefitWithConditions(
                condition("monthlyEarnedIncome", "==", "0")
        );
        UserProfile zeroIncomeProfile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                EmploymentStatus.UNEMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, zeroIncomeProfile);

        EligibilityResultDto zeroIncomeResult = eligibilityService.check(1L, 1L);

        UserProfile missingIncomeProfile = newProfile(
                LocalDate.now().minusYears(26), null,
                EmploymentStatus.UNEMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, missingIncomeProfile);
        EligibilityResultDto missingIncomeResult = eligibilityService.check(1L, 1L);

        assertThat(zeroIncomeResult.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(missingIncomeResult.getStatus()).isEqualTo(EligibilityStatus.NEED_MORE_INFO);
    }

    @Test
    void keepsLegacyAgeRegionAndBooleanConditionsCompatible() {
        Benefit benefit = benefitWithConditions(
                condition("age", "==", "26"),
                condition("region", "IN", "서울,경기"),
                condition("employed", "==", "true"),
                condition("student", "==", "false"),
                condition("houseOwner", "==", "false")
        );
        UserProfile profile = profileWithAddressAndEmployed("서울특별시 마포구", true);
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void evaluatesEmploymentTypeAndJobSeekingStatusLists() {
        Benefit benefit = benefitWithConditions(
                condition("employmentType", "IN", "CONTRACT, PART_TIME"),
                condition("jobSeekingStatus", "NOT_IN", "NOT_SEEKING")
        );
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                EmploymentStatus.EMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        profile.updateEmploymentType(EmploymentType.CONTRACT);
        profile.updateJobSeekingStatus(JobSeekingStatus.REGISTERED);
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void additionalEnumFieldsMoveFromMissingToEligibleOrIneligible() {
        assertAdditionalEnumTransitions(
                "employmentType", "IN", "CONTRACT,PART_TIME",
                EmploymentType.CONTRACT, EmploymentType.FULL_TIME
        );
        assertAdditionalEnumTransitions(
                "jobSeekingStatus", "IN", "REGISTERED,SEEKING_NOT_REGISTERED",
                JobSeekingStatus.REGISTERED, JobSeekingStatus.NOT_SEEKING
        );
    }

    @Test
    void distinguishesSmeEmployeeTrueFalseAndMissing() {
        Benefit benefit = benefitWithConditions(condition("smeEmployee", "==", "true"));
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                EmploymentStatus.EMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, profile);

        assertThat(eligibilityService.check(1L, 1L).getStatus())
                .isEqualTo(EligibilityStatus.NEED_MORE_INFO);

        profile.updateSmeEmployee(false);
        assertThat(eligibilityService.check(1L, 1L).getStatus())
                .isEqualTo(EligibilityStatus.INELIGIBLE);

        profile.updateSmeEmployee(true);
        assertThat(eligibilityService.check(1L, 1L).getStatus())
                .isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void calculatesGraduationMonthsAtExactBoundary() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-08-08T00:00:00Z"), ZoneOffset.UTC
        );
        eligibilityService = new EligibilityService(
                benefitRepository, userProfileRepository, fixedClock
        );
        Benefit benefit = benefitWithConditions(condition("graduationMonths", "<=", "24"));
        UserProfile profile = newProfile(
                LocalDate.of(2000, 1, 1), 0L,
                EmploymentStatus.UNEMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        profile.updateGraduationDate(LocalDate.of(2024, 8, 8));
        prepareRepositories(benefit, profile);

        assertThat(eligibilityService.calculateGraduationMonths(profile.getGraduationDate()))
                .isEqualTo(24L);
        assertThat(eligibilityService.check(1L, 1L).getStatus())
                .isEqualTo(EligibilityStatus.ELIGIBLE);

        profile.updateGraduationDate(LocalDate.of(2024, 7, 8));
        assertThat(eligibilityService.check(1L, 1L).getStatus())
                .isEqualTo(EligibilityStatus.INELIGIBLE);
    }

    @Test
    void missingGraduationDateRequiresMoreInformation() {
        Benefit benefit = benefitWithConditions(condition("graduationMonths", "<=", "24"));
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                EmploymentStatus.UNEMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, profile);

        EligibilityResultDto result = eligibilityService.check(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.NEED_MORE_INFO);
        assertThat(result.getConditionResults().getFirst().getMessage())
                .contains("졸업 시점");
    }

    private Benefit benefitWithConditions(BenefitCondition... conditions) {
        Benefit benefit = new Benefit(
                "청년 지원 정책",
                "설명",
                "지원 내용",
                "https://example.com",
                null
        );
        benefit.getConditions().addAll(java.util.List.of(conditions));
        return benefit;
    }

    private void assertAdditionalEnumTransitions(
            String fieldName,
            String operator,
            String conditionValue,
            Enum<?> passingValue,
            Enum<?> failingValue
    ) {
        Benefit benefit = benefitWithConditions(condition(fieldName, operator, conditionValue));
        UserProfile profile = newProfile(
                LocalDate.now().minusYears(26), 0L,
                EmploymentStatus.EMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME
        );
        prepareRepositories(benefit, profile);

        assertThat(eligibilityService.check(1L, 1L).getStatus())
                .isEqualTo(EligibilityStatus.NEED_MORE_INFO);

        updateAdditionalEnum(profile, passingValue);
        assertThat(eligibilityService.check(1L, 1L).getStatus())
                .isEqualTo(EligibilityStatus.ELIGIBLE);

        updateAdditionalEnum(profile, failingValue);
        assertThat(eligibilityService.check(1L, 1L).getStatus())
                .isEqualTo(EligibilityStatus.INELIGIBLE);
    }

    private void updateAdditionalEnum(UserProfile profile, Enum<?> value) {
        if (value instanceof EmploymentType employmentType) {
            profile.updateEmploymentType(employmentType);
        } else if (value instanceof JobSeekingStatus jobSeekingStatus) {
            profile.updateJobSeekingStatus(jobSeekingStatus);
        }
    }

    private BenefitCondition condition(String fieldName, String operator, String value) {
        return new BenefitCondition(fieldName, operator, value, true, null);
    }

    private UserProfile completeProfile() {
        return profileWithEmployed(true);
    }

    private UserProfile profileWithAddress(String address) {
        return profileWithAddressAndEmployed(address, true);
    }

    private UserProfile profileWithEmployed(Boolean employed) {
        return profileWithAddressAndEmployed("서울특별시 마포구", employed);
    }

    private UserProfile profileWithAddressAndEmployed(String address, Boolean employed) {
        return new UserProfile(
                26,
                address,
                1,
                1_800_000,
                21_600_000,
                82,
                employed,
                false,
                false,
                null
        );
    }

    private UserProfile newProfile(
            LocalDate birthDate,
            Long monthlyEarnedIncome,
            EmploymentStatus employmentStatus,
            EducationStatus educationStatus,
            HousingOwnershipStatus housingOwnershipStatus
    ) {
        return new UserProfile(
                birthDate,
                "11",
                1,
                monthlyEarnedIncome,
                0L,
                0L,
                0,
                employmentStatus,
                educationStatus,
                housingOwnershipStatus,
                null
        );
    }

    private void prepareRepositories(Benefit benefit, UserProfile profile) {
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
    }
}

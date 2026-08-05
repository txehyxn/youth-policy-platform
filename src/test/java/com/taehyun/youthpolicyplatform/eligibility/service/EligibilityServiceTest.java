package com.taehyun.youthpolicyplatform.eligibility.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityStatus;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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

    private BenefitCondition condition(String fieldName, String operator, String value) {
        return new BenefitCondition(fieldName, operator, value, true, null);
    }

    private UserProfile completeProfile() {
        return profileWithEmployed(true);
    }

    private UserProfile profileWithEmployed(Boolean employed) {
        return new UserProfile(
                26,
                "서울특별시 마포구",
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

    private void prepareRepositories(Benefit benefit, UserProfile profile) {
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
    }
}

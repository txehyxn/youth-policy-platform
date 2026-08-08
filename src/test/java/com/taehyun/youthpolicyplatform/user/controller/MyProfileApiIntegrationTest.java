package com.taehyun.youthpolicyplatform.user.controller;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.user.domain.EducationStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentType;
import com.taehyun.youthpolicyplatform.user.domain.HousingOwnershipStatus;
import com.taehyun.youthpolicyplatform.user.domain.JobSeekingStatus;
import com.taehyun.youthpolicyplatform.user.domain.Role;
import com.taehyun.youthpolicyplatform.user.domain.User;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import com.taehyun.youthpolicyplatform.user.repository.UserRepository;
import com.taehyun.youthpolicyplatform.user.util.IncomeCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyProfileApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    @Test
    void updatesOnlyProvidedBirthDate() throws Exception {
        String email = "patch-birth@example.com";
        UserProfile profile = saveCompleteProfile(email);
        LocalDate changedBirthDate = LocalDate.of(2001, 3, 15);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"birthDate\":\"2001-03-15\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileSaved").value(true));

        UserProfile updated = userProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(updated.getBirthDate()).isEqualTo(changedBirthDate);
        assertThat(updated.getRegionCode()).isEqualTo("서울특별시 마포구");
        assertThat(updated.getHouseholdSize()).isEqualTo(1);
        assertThat(updated.getMonthlyEarnedIncome()).isEqualTo(2_000_000L);
        assertThat(updated.getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
        assertThat(updated.getEducationStatus()).isEqualTo(EducationStatus.GRADUATED);
        assertThat(updated.getHousingOwnershipStatus()).isEqualTo(HousingOwnershipStatus.NO_HOME);
    }

    @Test
    void explicitNullClearsValueWhileEmptyRequestKeepsValues() throws Exception {
        String email = "patch-null@example.com";
        UserProfile profile = saveCompleteProfile(email);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        assertThat(userProfileRepository.findById(profile.getId()).orElseThrow()
                .getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employmentStatus\":null}"))
                .andExpect(status().isOk());

        assertThat(userProfileRepository.findById(profile.getId()).orElseThrow()
                .getEmploymentStatus()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "employmentStatus, EMPLOYED",
            "educationStatus, GRADUATED",
            "housingOwnershipStatus, NO_HOME"
    })
    void enumPatchImmediatelyChangesEligibilityResult(
            String requestField,
            String requestValue
    ) throws Exception {
        String email = "patch-" + requestField + "@example.com";
        saveEmptyProfile(email);
        Benefit benefit = saveBenefitWithCondition(
                requestField, "==", requestValue
        );

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"" + requestField + "\":\"" + requestValue + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.benefits[?(@.benefitId == " + benefit.getId() + ")].status"
                ).value(hasItem("ELIGIBLE")))
                .andExpect(jsonPath("$.summary.eligibleCount").value(1));
    }

    @Test
    void distinguishesZeroIncomeFromNullAndRecalculatesMiddleIncome() throws Exception {
        String email = "patch-income@example.com";
        UserProfile profile = saveEmptyProfile(email);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"householdSize\":1,\"monthlyEarnedIncome\":0}"))
                .andExpect(status().isOk());

        UserProfile zeroIncome = userProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(zeroIncome.getMonthlyEarnedIncome()).isZero();
        assertThat(zeroIncome.getMiddleIncomePercent()).isZero();

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monthlyEarnedIncome\":2000000}"))
                .andExpect(status().isOk());

        UserProfile withIncome = userProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(withIncome.getMiddleIncomePercent()).isEqualTo(
                IncomeCalculator.calculateMiddleIncomePercent(2_000_000L, 1)
        );

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monthlyEarnedIncome\":null}"))
                .andExpect(status().isOk());

        UserProfile missingIncome = userProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(missingIncome.getMonthlyEarnedIncome()).isNull();
        assertThat(missingIncome.getMiddleIncomePercent()).isNull();
    }

    @Test
    void returnsSummaryBenefitStatusesAndStructuredMissingFields() throws Exception {
        String email = "patch-summary@example.com";
        saveProfileWithBirthDate(email, LocalDate.now().minusYears(25));
        Benefit eligible = saveBenefitWithCondition("age", ">=", "19");
        Benefit needMoreInfo = saveBenefitWithCondition(
                "employmentStatus", "==", "EMPLOYED"
        );
        Benefit ineligible = saveBenefitWithCondition("age", "<=", "18");

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.eligibleCount").value(1))
                .andExpect(jsonPath("$.summary.needMoreInfoCount").value(1))
                .andExpect(jsonPath("$.summary.ineligibleCount").value(1))
                .andExpect(jsonPath(
                        "$.benefits[?(@.benefitId == " + eligible.getId() + ")].status"
                ).value(hasItem("ELIGIBLE")))
                .andExpect(jsonPath(
                        "$.benefits[?(@.benefitId == " + needMoreInfo.getId() + ")].status"
                ).value(hasItem("NEED_MORE_INFO")))
                .andExpect(jsonPath(
                        "$.benefits[?(@.benefitId == " + ineligible.getId() + ")].status"
                ).value(hasItem("INELIGIBLE")))
                .andExpect(jsonPath("$.missingFieldSummaries[0].field")
                        .value("EMPLOYMENT_STATUS"))
                .andExpect(jsonPath("$.missingFieldSummaries[0].affectedPolicyCount")
                        .value(1))
                .andExpect(jsonPath("$.missingFieldSummaries[0].singleMissingPolicyCount")
                        .value(1))
                .andExpect(jsonPath(
                        "$.benefits[?(@.benefitId == " + needMoreInfo.getId() + ")].missingFields[0]"
                ).value(hasItem("EMPLOYMENT_STATUS")));
    }

    @Test
    void distinguishesAffectedPoliciesFromSingleMissingPolicies() throws Exception {
        String email = "patch-missing-summary@example.com";
        saveEmptyProfile(email);
        saveBenefitWithCondition("employmentStatus", "==", "EMPLOYED");
        Benefit multipleMissing = new Benefit("복수 누락 정책", "설명", "지원", null, null);
        multipleMissing.getConditions().add(new BenefitCondition(
                "employmentStatus", "==", "EMPLOYED", true, multipleMissing
        ));
        multipleMissing.getConditions().add(new BenefitCondition(
                "educationStatus", "==", "GRADUATED", true, multipleMissing
        ));
        benefitRepository.save(multipleMissing);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.missingFieldSummaries[?(@.field == 'EMPLOYMENT_STATUS')].affectedPolicyCount"
                ).value(hasItem(2)))
                .andExpect(jsonPath(
                        "$.missingFieldSummaries[?(@.field == 'EMPLOYMENT_STATUS')].singleMissingPolicyCount"
                ).value(hasItem(1)))
                .andExpect(jsonPath(
                        "$.missingFieldSummaries[?(@.field == 'EDUCATION_STATUS')].affectedPolicyCount"
                ).value(hasItem(1)))
                .andExpect(jsonPath(
                        "$.missingFieldSummaries[?(@.field == 'EDUCATION_STATUS')].singleMissingPolicyCount"
                ).value(hasItem(0)));
    }

    @Test
    void mapsMissingAgeConditionToBirthDateInputField() throws Exception {
        String email = "patch-missing-birth@example.com";
        saveEmptyProfile(email);
        Benefit benefit = saveBenefitWithCondition("age", ">=", "19");

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.benefits[?(@.benefitId == " + benefit.getId() + ")].missingFields[0]"
                ).value(hasItem("BIRTH_DATE")))
                .andExpect(jsonPath("$.missingFieldSummaries[0].field")
                        .value("BIRTH_DATE"));
    }

    @Test
    void rejectsAnonymousRequestWithUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/my/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnknownEnumWithBadRequestJson() throws Exception {
        String email = "patch-invalid-enum@example.com";
        saveEmptyProfile(email);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employmentStatus\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_VALUE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "birthDate|{\"birthDate\":\"2999-01-01\"}",
            "monthlyEarnedIncome|{\"monthlyEarnedIncome\":-1}",
            "householdSize|{\"householdSize\":0}"
    })
    void rejectsInvalidProfileValues(
            String expectedField,
            String requestJson
    ) throws Exception {
        String email = "patch-invalid-" + expectedField + "@example.com";
        saveEmptyProfile(email);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PROFILE_FIELD"))
                .andExpect(jsonPath("$.field").value(expectedField))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void rejectsOverlyLongRegionAndAddress() throws Exception {
        String email = "patch-invalid-text@example.com";
        saveEmptyProfile(email);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"regionCode\":\"" + "가".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("regionCode"));

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"" + "나".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("address"));
    }

    @Test
    void patchesAdditionalProfileFieldsAndKeepsOtherValues() throws Exception {
        String email = "patch-additional@example.com";
        UserProfile profile = saveCompleteProfile(email);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "graduationDate":"2025-02-15",
                                  "employmentType":"CONTRACT",
                                  "smeEmployee":false,
                                  "jobSeekingStatus":"REGISTERED"
                                }
                                """))
                .andExpect(status().isOk());

        UserProfile updated = userProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(updated.getGraduationDate()).isEqualTo(LocalDate.of(2025, 2, 15));
        assertThat(updated.getEmploymentType()).isEqualTo(EmploymentType.CONTRACT);
        assertThat(updated.getSmeEmployee()).isFalse();
        assertThat(updated.getJobSeekingStatus()).isEqualTo(JobSeekingStatus.REGISTERED);
        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(updated.getMonthlyEarnedIncome()).isEqualTo(2_000_000L);
    }

    @Test
    void explicitNullClearsAdditionalFieldsAndKeepsFalseDistinct() throws Exception {
        String email = "patch-additional-null@example.com";
        UserProfile profile = saveCompleteProfile(email);
        profile.updateGraduationDate(LocalDate.of(2025, 2, 15));
        profile.updateEmploymentType(EmploymentType.FULL_TIME);
        profile.updateSmeEmployee(true);
        profile.updateJobSeekingStatus(JobSeekingStatus.REGISTERED);

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "graduationDate":null,
                                  "employmentType":null,
                                  "smeEmployee":false,
                                  "jobSeekingStatus":null
                                }
                                """))
                .andExpect(status().isOk());

        UserProfile updated = userProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(updated.getGraduationDate()).isNull();
        assertThat(updated.getEmploymentType()).isNull();
        assertThat(updated.getSmeEmployee()).isFalse();
        assertThat(updated.getJobSeekingStatus()).isNull();
    }

    @Test
    void mapsMissingGraduationMonthsToGraduationDate() throws Exception {
        String email = "patch-missing-graduation@example.com";
        saveEmptyProfile(email);
        Benefit benefit = saveBenefitWithCondition("graduationMonths", "<=", "24");

        mockMvc.perform(patch("/api/my/profile")
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.benefits[?(@.benefitId == " + benefit.getId() + ")].missingFields[0]"
                ).value(hasItem("GRADUATION_DATE")))
                .andExpect(jsonPath("$.missingFieldSummaries[0].field")
                        .value("GRADUATION_DATE"));
    }

    private UserProfile saveCompleteProfile(String email) {
        User user = saveUser(email);
        UserProfile profile = new UserProfile(
                LocalDate.of(2000, 1, 1),
                "서울특별시 마포구",
                1,
                2_000_000L,
                null,
                null,
                IncomeCalculator.calculateMiddleIncomePercent(2_000_000L, 1),
                EmploymentStatus.EMPLOYED,
                EducationStatus.GRADUATED,
                HousingOwnershipStatus.NO_HOME,
                user
        );
        profile.updateAddress("서울특별시 마포구 월드컵로");
        return userProfileRepository.save(profile);
    }

    private UserProfile saveEmptyProfile(String email) {
        return saveProfileWithBirthDate(email, null);
    }

    private UserProfile saveProfileWithBirthDate(String email, LocalDate birthDate) {
        User user = saveUser(email);
        return userProfileRepository.save(new UserProfile(
                birthDate, null, null, null, null, null,
                null, null, null, null, user
        ));
    }

    private User saveUser(String email) {
        return userRepository.save(new User(email, "test-password", Role.USER));
    }

    private Benefit saveBenefitWithCondition(
            String fieldName,
            String operator,
            String value
    ) {
        Benefit benefit = new Benefit("API 테스트 정책", "설명", "지원", null, null);
        benefit.getConditions().add(new BenefitCondition(
                fieldName, operator, value, true, benefit
        ));
        return benefitRepository.save(benefit);
    }
}

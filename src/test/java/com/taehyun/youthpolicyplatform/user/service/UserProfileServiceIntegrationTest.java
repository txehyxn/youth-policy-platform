package com.taehyun.youthpolicyplatform.user.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityStatus;
import com.taehyun.youthpolicyplatform.eligibility.service.EligibilityService;
import com.taehyun.youthpolicyplatform.user.domain.EducationStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentStatus;
import com.taehyun.youthpolicyplatform.user.domain.HousingOwnershipStatus;
import com.taehyun.youthpolicyplatform.user.domain.Role;
import com.taehyun.youthpolicyplatform.user.domain.User;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.dto.UserProfileRequest;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import com.taehyun.youthpolicyplatform.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserProfileServiceIntegrationTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    @Autowired
    private EligibilityService eligibilityService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void savesAndReloadsCoreProfileInputsIncludingZeroIncome() {
        User user = saveUser("profile-core@example.com");
        UserProfileRequest request = completeRequest();
        request.setMonthlyEarnedIncome(0L);

        UserProfile saved = userProfileService.saveProfileInputsForLoggedInUser(
                user.getEmail(), request
        );
        userProfileRepository.flush();
        entityManager.clear();

        UserProfile reloaded = userProfileRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getBirthDate()).isEqualTo(request.getBirthDate());
        assertThat(reloaded.getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
        assertThat(reloaded.getEducationStatus()).isEqualTo(EducationStatus.GRADUATED);
        assertThat(reloaded.getHousingOwnershipStatus()).isEqualTo(HousingOwnershipStatus.NO_HOME);
        assertThat(reloaded.getMonthlyEarnedIncome()).isZero();
        assertThat(reloaded.getRegionCode()).isEqualTo("서울특별시 마포구");
        assertThat(reloaded.getAddress()).isEqualTo("서울특별시 마포구 월드컵로");
    }

    @Test
    void preservesNullForUnenteredEnumsAndIncome() {
        User user = saveUser("profile-null@example.com");
        UserProfileRequest request = new UserProfileRequest();
        request.setAddress("   ");
        request.setRegionCode("");

        UserProfile saved = userProfileService.saveProfileInputsForLoggedInUser(
                user.getEmail(), request
        );
        userProfileRepository.flush();
        entityManager.clear();

        UserProfile reloaded = userProfileRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getBirthDate()).isNull();
        assertThat(reloaded.getEmploymentStatus()).isNull();
        assertThat(reloaded.getEducationStatus()).isNull();
        assertThat(reloaded.getHousingOwnershipStatus()).isNull();
        assertThat(reloaded.getMonthlyEarnedIncome()).isNull();
        assertThat(reloaded.getMiddleIncomePercent()).isNull();
        assertThat(reloaded.getAddress()).isNull();
        assertThat(reloaded.getRegionCode()).isNull();
    }

    @Test
    void updatesLegacyProfileWithoutRemovingAgeAndAnnualIncomeFallback() {
        User user = saveUser("profile-legacy@example.com");
        UserProfile legacyProfile = userProfileRepository.save(new UserProfile(
                29,
                "기존 주소",
                1,
                1_500_000,
                18_000_000,
                58,
                false,
                true,
                true,
                user
        ));
        UserProfileRequest request = completeRequest();
        request.setBirthDate(null);
        request.setMonthlyEarnedIncome(0L);

        UserProfile updated = userProfileService.saveProfileInputsForLoggedInUser(
                user.getEmail(), request
        );

        assertThat(updated.getId()).isEqualTo(legacyProfile.getId());
        assertThat(updated.getAge()).isEqualTo(29);
        assertThat(updated.getEligibilityAge()).isEqualTo(29);
        assertThat(updated.getAnnualIncome()).isEqualTo(18_000_000);
        assertThat(updated.getAddress()).isEqualTo(request.getAddress());
        assertThat(updated.getMonthlyEarnedIncome()).isZero();
        assertThat(updated.getMonthlyIncome()).isNull();
        assertThat(updated.getEmployed()).isNull();
        assertThat(updated.getStudent()).isNull();
        assertThat(updated.getHouseOwner()).isNull();
    }

    @Test
    void eligibilityUsesNewValuesAfterProfileIsSaved() {
        User user = saveUser("profile-eligibility@example.com");
        userProfileRepository.save(new UserProfile(
                50,
                "부산광역시",
                1,
                3_000_000,
                36_000_000,
                120,
                false,
                false,
                true,
                user
        ));
        UserProfileRequest request = completeRequest();
        request.setBirthDate(LocalDate.now().minusYears(25));
        request.setMonthlyEarnedIncome(0L);
        UserProfile savedProfile = userProfileService.saveProfileInputsForLoggedInUser(
                user.getEmail(), request
        );

        Benefit benefit = new Benefit("신규 프로필 판정 정책", "설명", "지원", null, null);
        benefit.getConditions().add(new BenefitCondition("age", "==", "25", true, benefit));
        benefit.getConditions().add(new BenefitCondition(
                "employmentStatus", "==", "EMPLOYED", true, benefit
        ));
        benefit.getConditions().add(new BenefitCondition(
                "educationStatus", "==", "GRADUATED", true, benefit
        ));
        benefit.getConditions().add(new BenefitCondition(
                "housingOwnershipStatus", "==", "NO_HOME", true, benefit
        ));
        benefit.getConditions().add(new BenefitCondition(
                "monthlyEarnedIncome", "==", "0", true, benefit
        ));
        benefit.getConditions().add(new BenefitCondition(
                "region", "IN", "서울,경기", true, benefit
        ));
        Benefit savedBenefit = benefitRepository.save(benefit);

        EligibilityResultDto result = eligibilityService.check(
                savedBenefit.getId(), savedProfile.getId()
        );

        assertThat(result.getStatus()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    private User saveUser(String email) {
        return userRepository.save(new User(email, "encoded-password", Role.USER));
    }

    private UserProfileRequest completeRequest() {
        UserProfileRequest request = new UserProfileRequest();
        request.setBirthDate(LocalDate.of(2000, 5, 10));
        request.setAddress("서울특별시 마포구 월드컵로");
        request.setRegionCode("서울특별시 마포구");
        request.setHouseholdSize(1);
        request.setMonthlyEarnedIncome(2_000_000L);
        request.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        request.setEducationStatus(EducationStatus.GRADUATED);
        request.setHousingOwnershipStatus(HousingOwnershipStatus.NO_HOME);
        return request;
    }
}

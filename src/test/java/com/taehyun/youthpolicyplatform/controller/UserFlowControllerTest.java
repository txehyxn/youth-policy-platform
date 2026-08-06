package com.taehyun.youthpolicyplatform.controller;

import com.taehyun.youthpolicyplatform.benefit.controller.BenefitUserController;
import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitSchedule;
import com.taehyun.youthpolicyplatform.benefit.dto.ScheduleDisplayDto;
import com.taehyun.youthpolicyplatform.benefit.service.BenefitService;
import com.taehyun.youthpolicyplatform.bookmark.service.BookmarkService;
import com.taehyun.youthpolicyplatform.eligibility.controller.EligibilityController;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityStatus;
import com.taehyun.youthpolicyplatform.eligibility.service.EligibilityService;
import com.taehyun.youthpolicyplatform.user.controller.MyProfileController;
import com.taehyun.youthpolicyplatform.user.controller.UserController;
import com.taehyun.youthpolicyplatform.user.domain.EducationStatus;
import com.taehyun.youthpolicyplatform.user.domain.EmploymentStatus;
import com.taehyun.youthpolicyplatform.user.domain.HousingOwnershipStatus;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.dto.SignupRequest;
import com.taehyun.youthpolicyplatform.user.dto.UserProfileRequest;
import com.taehyun.youthpolicyplatform.user.service.UserProfileService;
import com.taehyun.youthpolicyplatform.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserFlowControllerTest {

    @Test
    void signupErrorReturnsFormWithMessageAndEmail() {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        SignupRequest request = signupRequest("used@example.com");
        Model model = new ExtendedModelMap();

        doThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."))
                .when(userService).signup(request);

        String view = controller.signup(request, model);

        assertThat(view).isEqualTo("user/signup");
        assertThat(model.getAttribute("errorMessage"))
                .isEqualTo("이미 사용 중인 이메일입니다.");
        assertThat(model.getAttribute("email")).isEqualTo("used@example.com");
    }

    @Test
    void missingProfileKeepsBenefitIdForRegistrationFlow() {
        EligibilityService eligibilityService = mock(EligibilityService.class);
        UserProfileService profileService = mock(UserProfileService.class);
        BookmarkService bookmarkService = mock(BookmarkService.class);
        EligibilityController controller = new EligibilityController(
                eligibilityService,
                profileService,
                bookmarkService
        );
        Authentication authentication = authenticatedUser("user@example.com");
        Model model = new ExtendedModelMap();

        when(profileService.findByUserEmail("user@example.com"))
                .thenThrow(new IllegalArgumentException("프로필이 등록되지 않았습니다."));

        String view = controller.check(7L, authentication, model);

        assertThat(view).isEqualTo("eligibility/profile-required");
        assertThat(model.getAttribute("benefitId")).isEqualTo(7L);
    }

    @Test
    void eligibilityResultIncludesBookmarkState() {
        EligibilityService eligibilityService = mock(EligibilityService.class);
        UserProfileService profileService = mock(UserProfileService.class);
        BookmarkService bookmarkService = mock(BookmarkService.class);
        EligibilityController controller = new EligibilityController(
                eligibilityService,
                profileService,
                bookmarkService
        );
        Authentication authentication = authenticatedUser("user@example.com");
        UserProfile profile = mock(UserProfile.class);
        EligibilityResultDto result = new EligibilityResultDto(
                "청년 지원 정책",
                EligibilityStatus.ELIGIBLE,
                List.of()
        );
        Model model = new ExtendedModelMap();

        when(profile.getId()).thenReturn(3L);
        when(profileService.findByUserEmail("user@example.com")).thenReturn(profile);
        when(eligibilityService.check(7L, 3L)).thenReturn(result);
        when(bookmarkService.isBookmarked("user@example.com", 7L)).thenReturn(true);

        String view = controller.check(7L, authentication, model);

        assertThat(view).isEqualTo("eligibility/result");
        assertThat(model.getAttribute("result")).isSameAs(result);
        assertThat(model.getAttribute("isBookmarked")).isEqualTo(true);
    }

    @Test
    void profileSaveReturnsToOriginalBenefitWhenRequested() {
        UserProfileService profileService = mock(UserProfileService.class);
        BookmarkService bookmarkService = mock(BookmarkService.class);
        MyProfileController controller = new MyProfileController(profileService, bookmarkService);
        Authentication authentication = authenticatedUser("user@example.com");
        UserProfileRequest request = new UserProfileRequest();
        request.setBirthDate(LocalDate.of(2001, 5, 10));
        request.setAddress("서울특별시 마포구");
        request.setRegionCode("서울특별시 마포구");
        request.setHouseholdSize(1);
        request.setMonthlyEarnedIncome(2_000_000L);
        request.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        request.setEducationStatus(EducationStatus.GRADUATED);
        request.setHousingOwnershipStatus(HousingOwnershipStatus.NO_HOME);

        String view = controller.saveProfile(
                authentication,
                request,
                7L
        );

        assertThat(view).isEqualTo("redirect:/benefits/7?profileSaved=true");
        verify(profileService).saveProfileInputsForLoggedInUser(
                "user@example.com", request
        );
    }

    @Test
    void profileFormBindsBlankOptionalValuesAsNull() throws Exception {
        UserProfileService profileService = mock(UserProfileService.class);
        BookmarkService bookmarkService = mock(BookmarkService.class);
        MyProfileController controller = new MyProfileController(profileService, bookmarkService);
        Authentication authentication = authenticatedUser("user@example.com");
        MockMvc mockMvc = standaloneSetup(controller).build();

        mockMvc.perform(post("/my/profile")
                        .principal(authentication)
                        .param("birthDate", "")
                        .param("monthlyEarnedIncome", "")
                        .param("employmentStatus", "")
                        .param("educationStatus", "")
                        .param("housingOwnershipStatus", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my/profile?saved=true"));

        org.mockito.ArgumentCaptor<UserProfileRequest> requestCaptor =
                org.mockito.ArgumentCaptor.forClass(UserProfileRequest.class);
        verify(profileService).saveProfileInputsForLoggedInUser(
                org.mockito.ArgumentMatchers.eq("user@example.com"),
                requestCaptor.capture()
        );

        UserProfileRequest request = requestCaptor.getValue();
        assertThat(request.getBirthDate()).isNull();
        assertThat(request.getMonthlyEarnedIncome()).isNull();
        assertThat(request.getEmploymentStatus()).isNull();
        assertThat(request.getEducationStatus()).isNull();
        assertThat(request.getHousingOwnershipStatus()).isNull();
    }

    @Test
    void alwaysOpenScheduleIsSortedWithoutNullException() {
        BenefitService benefitService = mock(BenefitService.class);
        BookmarkService bookmarkService = mock(BookmarkService.class);
        EligibilityService eligibilityService = mock(EligibilityService.class);
        UserProfileService profileService = mock(UserProfileService.class);
        BenefitUserController controller = new BenefitUserController(
                benefitService,
                bookmarkService,
                eligibilityService,
                profileService
        );
        Benefit benefit = new Benefit("정책", "설명", "지원", "https://example.com", null);
        LocalDate startDate = LocalDate.of(2026, 9, 1);

        benefit.getSchedules().add(new BenefitSchedule(
                "상시 일정", null, null, "상시", true, benefit
        ));
        benefit.getSchedules().add(new BenefitSchedule(
                "일반 일정", startDate, startDate.plusDays(10), "기간", false, benefit
        ));
        when(benefitService.findById(1L)).thenReturn(benefit);
        Model model = new ExtendedModelMap();

        String view = controller.benefitDetail(1L, null, model);

        @SuppressWarnings("unchecked")
        List<ScheduleDisplayDto> schedules =
                (List<ScheduleDisplayDto>) model.getAttribute("schedules");

        assertThat(view).isEqualTo("benefit/detail");
        assertThat(schedules).extracting(ScheduleDisplayDto::getStartDate)
                .containsExactly(startDate, null);
    }

    @Test
    void anonymousBenefitListShowsLoginGuideAndKeepsPoliciesVisible() {
        BenefitService benefitService = mock(BenefitService.class);
        BenefitUserController controller = benefitListController(
                benefitService,
                mock(EligibilityService.class),
                mock(UserProfileService.class)
        );
        Benefit benefit = mockBenefit(1L);
        when(benefitService.findAllForEligibility()).thenReturn(List.of(benefit));
        Model model = new ExtendedModelMap();

        String view = controller.benefitList(null, model);

        assertThat(view).isEqualTo("benefit/list");
        assertThat(model.getAttribute("loginRequired")).isEqualTo(true);
        assertThat(model.getAttribute("benefits")).isEqualTo(List.of(benefit));
    }

    @Test
    void benefitListShowsProfileGuideWhenLoggedInUserHasNoProfile() {
        BenefitService benefitService = mock(BenefitService.class);
        EligibilityService eligibilityService = mock(EligibilityService.class);
        UserProfileService profileService = mock(UserProfileService.class);
        BenefitUserController controller = benefitListController(
                benefitService,
                eligibilityService,
                profileService
        );
        Authentication authentication = authenticatedUser("user@example.com");
        Benefit benefit = mockBenefit(1L);
        when(benefitService.findAllForEligibility()).thenReturn(List.of(benefit));
        when(profileService.findByUserEmail("user@example.com"))
                .thenThrow(new IllegalArgumentException("프로필이 등록되지 않았습니다."));
        Model model = new ExtendedModelMap();

        String view = controller.benefitList(authentication, model);

        assertThat(view).isEqualTo("benefit/list");
        assertThat(model.getAttribute("profileRequired")).isEqualTo(true);
        assertThat(model.getAttribute("eligibilityResults")).isNull();
    }

    @Test
    void personalizedBenefitListSortsEligibleNeedMoreInfoAndIneligible() {
        BenefitService benefitService = mock(BenefitService.class);
        EligibilityService eligibilityService = mock(EligibilityService.class);
        UserProfileService profileService = mock(UserProfileService.class);
        BenefitUserController controller = benefitListController(
                benefitService,
                eligibilityService,
                profileService
        );
        Benefit ineligible = mockBenefit(3L);
        Benefit eligible = mockBenefit(1L);
        Benefit needMoreInfo = mockBenefit(2L);
        List<Benefit> originalOrder = List.of(ineligible, eligible, needMoreInfo);
        UserProfile profile = mock(UserProfile.class);
        Authentication authentication = authenticatedUser("user@example.com");
        Map<Long, EligibilityResultDto> results = new LinkedHashMap<>();
        results.put(3L, result(3L, EligibilityStatus.INELIGIBLE));
        results.put(1L, result(1L, EligibilityStatus.ELIGIBLE));
        results.put(2L, result(2L, EligibilityStatus.NEED_MORE_INFO));

        when(benefitService.findAllForEligibility()).thenReturn(originalOrder);
        when(profileService.findByUserEmail("user@example.com")).thenReturn(profile);
        when(eligibilityService.checkAll(originalOrder, profile)).thenReturn(results);
        Model model = new ExtendedModelMap();

        String view = controller.benefitList(authentication, model);

        @SuppressWarnings("unchecked")
        List<Benefit> sortedBenefits = (List<Benefit>) model.getAttribute("benefits");

        assertThat(view).isEqualTo("benefit/list");
        assertThat(model.getAttribute("personalized")).isEqualTo(true);
        assertThat(sortedBenefits).extracting(Benefit::getId)
                .containsExactly(1L, 2L, 3L);
        assertThat(model.getAttribute("eligibilityResults")).isSameAs(results);
    }

    private BenefitUserController benefitListController(
            BenefitService benefitService,
            EligibilityService eligibilityService,
            UserProfileService profileService
    ) {
        return new BenefitUserController(
                benefitService,
                mock(BookmarkService.class),
                eligibilityService,
                profileService
        );
    }

    private Benefit mockBenefit(Long id) {
        Benefit benefit = mock(Benefit.class);
        when(benefit.getId()).thenReturn(id);
        return benefit;
    }

    private EligibilityResultDto result(Long benefitId, EligibilityStatus status) {
        return new EligibilityResultDto(
                benefitId,
                "정책 " + benefitId,
                "카테고리",
                "지원 내용",
                null,
                status,
                List.of()
        );
    }

    private SignupRequest signupRequest(String email) {
        SignupRequest request = new SignupRequest();
        request.setEmail(email);
        request.setPassword("password");
        request.setPasswordConfirm("password");
        return request;
    }

    private Authentication authenticatedUser(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(email);
        when(authentication.getName()).thenReturn(email);
        return authentication;
    }
}

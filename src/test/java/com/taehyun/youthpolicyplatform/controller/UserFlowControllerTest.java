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
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.dto.SignupRequest;
import com.taehyun.youthpolicyplatform.user.service.UserProfileService;
import com.taehyun.youthpolicyplatform.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        String view = controller.saveProfile(
                authentication,
                25,
                "서울특별시 마포구",
                1,
                2_000_000,
                24_000_000,
                true,
                false,
                false,
                7L
        );

        assertThat(view).isEqualTo("redirect:/benefits/7?profileSaved=true");
        verify(profileService).saveForLoggedInUser(
                "user@example.com",
                25,
                "서울특별시 마포구",
                1,
                2_000_000,
                24_000_000,
                true,
                false,
                false
        );
    }

    @Test
    void alwaysOpenScheduleIsSortedWithoutNullException() {
        BenefitService benefitService = mock(BenefitService.class);
        BookmarkService bookmarkService = mock(BookmarkService.class);
        BenefitUserController controller = new BenefitUserController(benefitService, bookmarkService);
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

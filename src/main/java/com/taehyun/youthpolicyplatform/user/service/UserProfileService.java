package com.taehyun.youthpolicyplatform.user.service;

import com.taehyun.youthpolicyplatform.user.domain.Role;
import com.taehyun.youthpolicyplatform.user.domain.User;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.dto.UserProfilePatchRequest;
import com.taehyun.youthpolicyplatform.user.dto.UserProfileRequest;
import com.taehyun.youthpolicyplatform.user.exception.ProfileValidationException;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import com.taehyun.youthpolicyplatform.user.repository.UserRepository;
import com.taehyun.youthpolicyplatform.user.util.IncomeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 관리자용 프로필 등록
    public UserProfile save(
            Integer age,
            String address,
            Integer householdSize,
            Integer monthlyIncome,
            Integer annualIncome,
            Boolean employed,
            Boolean student,
            Boolean houseOwner,
            Long userId
    ) {

        User user = userRepository.findById(userId)
                .orElseGet(() -> userRepository.save(
                        new User("test@test.com", "test1234", Role.USER)
                ));

        // 중위소득 %는 직접 입력받지 않고, 월소득과 가구원 수로 자동 계산한다
        Integer middleIncomePercent =
                IncomeCalculator.calculateMiddleIncomePercent(monthlyIncome, householdSize);

        UserProfile profile = new UserProfile(
                age,
                address,
                householdSize,
                monthlyIncome,
                annualIncome,
                middleIncomePercent,
                employed,
                student,
                houseOwner,
                user
        );

        return userProfileRepository.save(profile);
    }

    // 전체 조회
    public List<UserProfile> findAll() {
        return userProfileRepository.findAll();
    }

    // 로그인한 사용자 이메일로 프로필 조회
    public UserProfile findByUserEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return userProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 등록되지 않았습니다."));
    }

    // 로그인한 사용자 프로필 등록 또는 수정
    public UserProfile saveForLoggedInUser(
            String email,
            Integer age,
            String address,
            Integer householdSize,
            Integer monthlyIncome,
            Integer annualIncome,
            Boolean employed,
            Boolean student,
            Boolean houseOwner
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 중위소득 %는 직접 입력받지 않고, 월소득과 가구원 수로 자동 계산한다
        Integer middleIncomePercent =
                IncomeCalculator.calculateMiddleIncomePercent(monthlyIncome, householdSize);

        return userProfileRepository.findByUser(user)
                .map(profile -> {
                    profile.update(
                            age,
                            address,
                            householdSize,
                            monthlyIncome,
                            annualIncome,
                            middleIncomePercent,
                            employed,
                            student,
                            houseOwner
                    );

                    return userProfileRepository.save(profile);
                })
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile(
                            age,
                            address,
                            householdSize,
                            monthlyIncome,
                            annualIncome,
                            middleIncomePercent,
                            employed,
                            student,
                            houseOwner,
                            user
                    );

                    return userProfileRepository.save(profile);
                });
    }

    public UserProfile saveProfileInputsForLoggedInUser(
            String email,
            UserProfileRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String address = trimToNull(request.getAddress());
        String regionCode = trimToNull(request.getRegionCode());
        Integer middleIncomePercent = IncomeCalculator.calculateMiddleIncomePercent(
                request.getMonthlyEarnedIncome(),
                request.getHouseholdSize()
        );

        return userProfileRepository.findByUser(user)
                .map(profile -> {
                    profile.updateProfileInputs(
                            request.getBirthDate(),
                            address,
                            regionCode,
                            request.getHouseholdSize(),
                            request.getMonthlyEarnedIncome(),
                            middleIncomePercent,
                            request.getEmploymentStatus(),
                            request.getEducationStatus(),
                            request.getHousingOwnershipStatus()
                    );
                    return userProfileRepository.save(profile);
                })
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile(
                            request.getBirthDate(),
                            regionCode,
                            request.getHouseholdSize(),
                            request.getMonthlyEarnedIncome(),
                            null,
                            null,
                            middleIncomePercent,
                            request.getEmploymentStatus(),
                            request.getEducationStatus(),
                            request.getHousingOwnershipStatus(),
                            user
                    );
                    profile.updateProfileInputs(
                            request.getBirthDate(),
                            address,
                            regionCode,
                            request.getHouseholdSize(),
                            request.getMonthlyEarnedIncome(),
                            middleIncomePercent,
                            request.getEmploymentStatus(),
                            request.getEducationStatus(),
                            request.getHousingOwnershipStatus()
                    );
                    return userProfileRepository.save(profile);
                });
    }

    @Transactional
    public UserProfile patchProfileInputsForLoggedInUser(
            String email,
            UserProfilePatchRequest request
    ) {
        validatePatchRequest(request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> new UserProfile(
                        null, null, null, null, null, null,
                        null, null, null, null, user
                ));

        if (request.hasField("birthDate")) {
            profile.updateBirthDate(request.getBirthDate());
        }
        if (request.hasField("address")) {
            profile.updateAddress(trimToNull(request.getAddress()));
        }
        if (request.hasField("regionCode")) {
            profile.updateRegionCode(trimToNull(request.getRegionCode()));
        }
        if (request.hasField("householdSize")) {
            profile.updateHouseholdSize(request.getHouseholdSize());
        }
        if (request.hasField("monthlyEarnedIncome")) {
            profile.updateMonthlyEarnedIncome(request.getMonthlyEarnedIncome());
        }
        if (request.hasField("employmentStatus")) {
            profile.updateEmploymentStatus(request.getEmploymentStatus());
        }
        if (request.hasField("educationStatus")) {
            profile.updateEducationStatus(request.getEducationStatus());
        }
        if (request.hasField("housingOwnershipStatus")) {
            profile.updateHousingOwnershipStatus(request.getHousingOwnershipStatus());
        }

        if (request.hasField("householdSize")
                || request.hasField("monthlyEarnedIncome")) {
            profile.updateMiddleIncomePercent(
                    IncomeCalculator.calculateMiddleIncomePercent(
                            profile.getEligibilityMonthlyEarnedIncome(),
                            profile.getHouseholdSize()
                    )
            );
        }

        return userProfileRepository.save(profile);
    }

    private void validatePatchRequest(UserProfilePatchRequest request) {
        if (request.hasField("birthDate")
                && request.getBirthDate() != null
                && request.getBirthDate().isAfter(LocalDate.now())) {
            throw new ProfileValidationException(
                    "birthDate", "생년월일은 미래 날짜일 수 없습니다."
            );
        }
        if (request.hasField("householdSize")
                && request.getHouseholdSize() != null
                && request.getHouseholdSize() < 1) {
            throw new ProfileValidationException(
                    "householdSize", "가구원 수는 1명 이상이어야 합니다."
            );
        }
        if (request.hasField("monthlyEarnedIncome")
                && request.getMonthlyEarnedIncome() != null
                && request.getMonthlyEarnedIncome() < 0) {
            throw new ProfileValidationException(
                    "monthlyEarnedIncome", "월 근로·사업소득은 0원 이상이어야 합니다."
            );
        }
        validateTextLength(request, "regionCode", request.getRegionCode(), 100);
        validateTextLength(request, "address", request.getAddress(), 255);
    }

    private void validateTextLength(
            UserProfilePatchRequest request,
            String field,
            String value,
            int maxLength
    ) {
        if (request.hasField(field) && value != null && value.length() > maxLength) {
            throw new ProfileValidationException(
                    field, field + " 값은 " + maxLength + "자 이하여야 합니다."
            );
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

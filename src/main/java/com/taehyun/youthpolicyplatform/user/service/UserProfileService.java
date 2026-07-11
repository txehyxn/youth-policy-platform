package com.taehyun.youthpolicyplatform.user.service;

import com.taehyun.youthpolicyplatform.user.domain.Role;
import com.taehyun.youthpolicyplatform.user.domain.User;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import com.taehyun.youthpolicyplatform.user.repository.UserRepository;
import com.taehyun.youthpolicyplatform.user.util.IncomeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
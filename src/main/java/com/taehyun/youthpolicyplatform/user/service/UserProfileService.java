package com.taehyun.youthpolicyplatform.user.service;

import com.taehyun.youthpolicyplatform.user.domain.Role;
import com.taehyun.youthpolicyplatform.user.domain.User;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import com.taehyun.youthpolicyplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 프로필 등록
    public UserProfile save(
            Integer age,
            String region,
            Integer householdSize,
            Integer monthlyIncome,
            Integer annualIncome,
            Integer middleIncomePercent,
            Boolean employed,
            Boolean student,
            Boolean houseOwner,
            Long userId
    ) {

        // 테스트 단계에서는 사용자가 없으면 임시 사용자를 생성한다
        User user = userRepository.findById(userId)
                .orElseGet(() -> userRepository.save(
                        new User("test@test.com", "test1234", Role.USER)
                ));

        UserProfile profile = new UserProfile(
                age,
                region,
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
}
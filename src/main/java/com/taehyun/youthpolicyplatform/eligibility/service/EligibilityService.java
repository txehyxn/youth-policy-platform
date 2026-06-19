package com.taehyun.youthpolicyplatform.eligibility.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityConditionResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final BenefitRepository benefitRepository;
    private final UserProfileRepository userProfileRepository;

    public EligibilityResultDto check(Long benefitId, Long profileId) {

        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));

        List<EligibilityConditionResultDto> conditionResults = new ArrayList<>();

        boolean eligible = true;

        for (BenefitCondition condition : benefit.getConditions()) {

            boolean passed = checkCondition(profile, condition);

            if (!passed) {
                eligible = false;
            }

            conditionResults.add(
                    new EligibilityConditionResultDto(
                            condition.getFieldName(),
                            condition.getOperator(),
                            condition.getValue(),
                            passed
                    )
            );
        }

        return new EligibilityResultDto(
                benefit.getTitle(),
                eligible,
                conditionResults
        );
    }

    private boolean checkCondition(UserProfile profile, BenefitCondition condition) {

        String fieldName = condition.getFieldName();
        String operator = condition.getOperator();
        String value = condition.getValue();

        if (fieldName.equals("age")) {
            return compareNumber(profile.getAge(), operator, Integer.parseInt(value));
        }

        if (fieldName.equals("middle_income_percent")) {
            return compareNumber(profile.getMiddleIncomePercent(), operator, Integer.parseInt(value));
        }

        if (fieldName.equals("house_owner")) {
            return compareBoolean(profile.getHouseOwner(), operator, Boolean.parseBoolean(value));
        }

        return false;
    }

    private boolean compareNumber(Integer userValue, String operator, Integer conditionValue) {

        if (operator.equals(">=")) {
            return userValue >= conditionValue;
        }

        if (operator.equals("<=")) {
            return userValue <= conditionValue;
        }

        if (operator.equals("=")) {
            return userValue.equals(conditionValue);
        }

        return false;
    }

    private boolean compareBoolean(Boolean userValue, String operator, Boolean conditionValue) {

        if (operator.equals("=")) {
            return userValue.equals(conditionValue);
        }

        return false;
    }
}
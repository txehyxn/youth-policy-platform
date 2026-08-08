package com.taehyun.youthpolicyplatform.eligibility.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.service.BenefitService;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityResultDto;
import com.taehyun.youthpolicyplatform.eligibility.dto.EligibilityStatus;
import com.taehyun.youthpolicyplatform.eligibility.dto.ProfileEligibilityUpdateResponse;
import com.taehyun.youthpolicyplatform.user.domain.ProfileField;
import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import com.taehyun.youthpolicyplatform.user.dto.UserProfilePatchRequest;
import com.taehyun.youthpolicyplatform.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileEligibilityUpdateService {

    private final UserProfileService userProfileService;
    private final BenefitService benefitService;
    private final EligibilityService eligibilityService;

    @Transactional
    public ProfileEligibilityUpdateResponse updateAndRecalculate(
            String email,
            UserProfilePatchRequest request
    ) {
        UserProfile profile = userProfileService.patchProfileInputsForLoggedInUser(
                email, request
        );
        return recalculate(profile, true);
    }

    @Transactional(readOnly = true)
    public ProfileEligibilityUpdateResponse recalculate(UserProfile profile) {
        return recalculate(profile, false);
    }

    private ProfileEligibilityUpdateResponse recalculate(
            UserProfile profile,
            boolean profileSaved
    ) {
        List<Benefit> benefits = benefitService.findAllForEligibility();
        Map<Long, EligibilityResultDto> results = eligibilityService.checkAll(
                benefits, profile
        );

        int eligibleCount = 0;
        int needMoreInfoCount = 0;
        int ineligibleCount = 0;
        List<ProfileEligibilityUpdateResponse.BenefitStatus> benefitStatuses =
                new ArrayList<>();
        Map<ProfileField, int[]> missingCounts = new EnumMap<>(ProfileField.class);

        for (EligibilityResultDto result : results.values()) {
            if (result.getStatus() == EligibilityStatus.ELIGIBLE) {
                eligibleCount++;
            } else if (result.getStatus() == EligibilityStatus.NEED_MORE_INFO) {
                needMoreInfoCount++;
            } else {
                ineligibleCount++;
            }

            List<ProfileField> missingFields = findMissingFields(result);
            benefitStatuses.add(new ProfileEligibilityUpdateResponse.BenefitStatus(
                    result.getBenefitId(), result.getStatus(), missingFields
            ));

            for (ProfileField field : missingFields) {
                int[] counts = missingCounts.computeIfAbsent(field, ignored -> new int[2]);
                counts[0]++;
                if (missingFields.size() == 1) {
                    counts[1]++;
                }
            }
        }

        List<ProfileEligibilityUpdateResponse.MissingFieldSummary> missingSummaries =
                missingCounts.entrySet().stream()
                        .map(entry -> new ProfileEligibilityUpdateResponse.MissingFieldSummary(
                                entry.getKey(), entry.getValue()[0], entry.getValue()[1]
                        ))
                        .sorted((left, right) -> {
                            int singleMissingCompare = Integer.compare(
                                    right.singleMissingPolicyCount(),
                                    left.singleMissingPolicyCount()
                            );
                            if (singleMissingCompare != 0) {
                                return singleMissingCompare;
                            }
                            return Integer.compare(
                                    right.affectedPolicyCount(),
                                    left.affectedPolicyCount()
                            );
                        })
                        .toList();

        return new ProfileEligibilityUpdateResponse(
                profileSaved,
                new ProfileEligibilityUpdateResponse.Summary(
                        eligibleCount, needMoreInfoCount, ineligibleCount
                ),
                missingSummaries,
                benefitStatuses
        );
    }

    private List<ProfileField> findMissingFields(EligibilityResultDto result) {
        if (result.getStatus() != EligibilityStatus.NEED_MORE_INFO) {
            return List.of();
        }

        return result.getConditionResults().stream()
                .filter(condition -> condition.getStatus() == EligibilityStatus.NEED_MORE_INFO)
                .map(condition -> ProfileField.fromKey(condition.getFieldName())
                        .map(this::toInputField)
                        .orElse(null))
                .filter(field -> field != null)
                .distinct()
                .toList();
    }

    private ProfileField toInputField(ProfileField conditionField) {
        if (conditionField == ProfileField.AGE) {
            return ProfileField.BIRTH_DATE;
        }
        if (conditionField == ProfileField.GRADUATION_MONTHS) {
            return ProfileField.GRADUATION_DATE;
        }
        return conditionField;
    }
}

package com.taehyun.youthpolicyplatform.eligibility.dto;

import com.taehyun.youthpolicyplatform.user.domain.ProfileField;

import java.util.List;

public record ProfileEligibilityUpdateResponse(
        boolean profileSaved,
        Summary summary,
        List<MissingFieldSummary> missingFieldSummaries,
        List<BenefitStatus> benefits
) {

    public record Summary(
            int eligibleCount,
            int needMoreInfoCount,
            int ineligibleCount
    ) {
    }

    public record MissingFieldSummary(
            ProfileField field,
            int affectedPolicyCount,
            int singleMissingPolicyCount
    ) {
    }

    public record BenefitStatus(
            Long benefitId,
            EligibilityStatus status,
            List<ProfileField> missingFields
    ) {
    }
}

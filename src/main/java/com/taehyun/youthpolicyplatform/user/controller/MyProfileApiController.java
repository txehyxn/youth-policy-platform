package com.taehyun.youthpolicyplatform.user.controller;

import com.taehyun.youthpolicyplatform.eligibility.dto.ProfileEligibilityUpdateResponse;
import com.taehyun.youthpolicyplatform.eligibility.service.ProfileEligibilityUpdateService;
import com.taehyun.youthpolicyplatform.user.dto.UserProfilePatchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my/profile")
@RequiredArgsConstructor
public class MyProfileApiController {

    private final ProfileEligibilityUpdateService profileEligibilityUpdateService;

    @PatchMapping
    public ProfileEligibilityUpdateResponse updateProfile(
            Authentication authentication,
            @RequestBody UserProfilePatchRequest request
    ) {
        return profileEligibilityUpdateService.updateAndRecalculate(
                authentication.getName(), request
        );
    }
}

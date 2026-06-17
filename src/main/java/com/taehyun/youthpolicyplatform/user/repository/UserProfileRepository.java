package com.taehyun.youthpolicyplatform.user.repository;

import com.taehyun.youthpolicyplatform.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
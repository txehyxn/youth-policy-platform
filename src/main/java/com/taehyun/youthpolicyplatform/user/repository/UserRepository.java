package com.taehyun.youthpolicyplatform.user.repository;

import com.taehyun.youthpolicyplatform.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 회원 데이터 접근 Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}
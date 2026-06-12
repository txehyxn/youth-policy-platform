package com.taehyun.youthpolicyplatform.user.repository;

import com.taehyun.youthpolicyplatform.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

// 회원 데이터 접근 Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
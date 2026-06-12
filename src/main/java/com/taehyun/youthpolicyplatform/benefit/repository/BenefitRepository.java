package com.taehyun.youthpolicyplatform.benefit.repository;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;

// 정책 데이터 접근 Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {

}
package com.taehyun.youthpolicyplatform.benefit.repository;

import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitConditionRepository extends JpaRepository<BenefitCondition, Long> {
}
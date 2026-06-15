package com.taehyun.youthpolicyplatform.benefit.repository;

import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitCategoryRepository extends JpaRepository<BenefitCategory, Long> {
}
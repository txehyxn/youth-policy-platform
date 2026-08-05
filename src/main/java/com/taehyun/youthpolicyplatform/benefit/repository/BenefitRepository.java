package com.taehyun.youthpolicyplatform.benefit.repository;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// 정책 데이터 접근 Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {

    @EntityGraph(attributePaths = {"category", "conditions"})
    @Query("select distinct benefit from Benefit benefit")
    List<Benefit> findAllForEligibility();
}

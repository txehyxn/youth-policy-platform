package com.taehyun.youthpolicyplatform.benefit.repository;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BenefitScheduleRepository
        extends JpaRepository<BenefitSchedule, Long> {

    List<BenefitSchedule> findByBenefit(Benefit benefit);

}
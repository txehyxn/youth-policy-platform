package com.taehyun.youthpolicyplatform.benefit.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitSchedule;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BenefitScheduleService {

    private final BenefitScheduleRepository benefitScheduleRepository;
    private final BenefitRepository benefitRepository;

    // 정책별 신청 일정 등록
    public BenefitSchedule save(
            Long benefitId,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String description
    ) {
        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        BenefitSchedule schedule = new BenefitSchedule(
                title,
                startDate,
                endDate,
                description,
                benefit
        );

        return benefitScheduleRepository.save(schedule);
    }

    // 특정 정책의 신청 일정 조회
    public List<BenefitSchedule> findByBenefit(Benefit benefit) {
        return benefitScheduleRepository.findByBenefit(benefit);
    }

    // 신청 일정 삭제
    public void delete(Long id) {
        benefitScheduleRepository.deleteById(id);
    }
}
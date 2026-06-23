package com.taehyun.youthpolicyplatform.benefit.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCondition;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitConditionRepository;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// 정책 조건 관련 비즈니스 로직을 처리하는 서비스
@Service
@RequiredArgsConstructor
public class BenefitConditionService {

    private final BenefitConditionRepository benefitConditionRepository;
    private final BenefitRepository benefitRepository;

    // 정책 조건 등록
    public BenefitCondition save(
            Long benefitId,
            String fieldName,
            String operator,
            String value,
            Boolean required
    ) {
        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        BenefitCondition condition = new BenefitCondition(
                fieldName,
                operator,
                value,
                required,
                benefit
        );

        return benefitConditionRepository.save(condition);
    }

    // 전체 정책 조건 조회
    public List<BenefitCondition> findAll() {
        return benefitConditionRepository.findAll();
    }

    // 정책 조건 삭제
    public void delete(Long id) {
        benefitConditionRepository.deleteById(id);
    }
}
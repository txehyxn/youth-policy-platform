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

    // ===== 기존 방식 (범용) - 그대로 유지 =====
    public BenefitCondition save(
            Long benefitId,
            String fieldName,
            String operator,
            String value,
            Boolean required
    ) {
        Benefit benefit = findBenefit(benefitId);

        BenefitCondition condition = new BenefitCondition(
                fieldName,
                operator,
                value,
                required,
                benefit
        );

        return benefitConditionRepository.save(condition);
    }

    // ===== 사람이 알기 쉬운 방식 (신규) =====

    // 나이: "몇 세부터 ~ 몇 세까지"를 입력받아서, 내부적으로 age >= 최소 / age <= 최대 조건을 만든다
    public void saveAgeRange(
            Long benefitId,
            Integer minAge,
            Integer maxAge,
            Boolean required
    ) {
        Benefit benefit = findBenefit(benefitId);

        if (minAge != null) {
            benefitConditionRepository.save(
                    new BenefitCondition("age", ">=", minAge.toString(), required, benefit)
            );
        }

        if (maxAge != null) {
            benefitConditionRepository.save(
                    new BenefitCondition("age", "<=", maxAge.toString(), required, benefit)
            );
        }
    }

    // 지역: 시/도를 선택받아서, 내부적으로 region == 선택값 조건을 만든다
    public void saveRegion(
            Long benefitId,
            String region,
            Boolean required
    ) {
        Benefit benefit = findBenefit(benefitId);

        benefitConditionRepository.save(
                new BenefitCondition("region", "==", region, required, benefit)
        );
    }

    // 중위소득: "몇 % 이하"만 입력받아서, 내부적으로 middleIncomePercent <= 값 조건을 만든다
    public void saveMiddleIncomePercent(
            Long benefitId,
            Integer maxPercent,
            Boolean required
    ) {
        Benefit benefit = findBenefit(benefitId);

        benefitConditionRepository.save(
                new BenefitCondition(
                        "middleIncomePercent",
                        "<=",
                        maxPercent.toString(),
                        required,
                        benefit
                )
        );
    }

    // 취업/학생/자가보유 여부: O/X만 입력받아서, 내부적으로 항목 == true/false 조건을 만든다
    public void saveBooleanCondition(
            Long benefitId,
            String fieldName,
            Boolean value,
            Boolean required
    ) {
        Benefit benefit = findBenefit(benefitId);

        benefitConditionRepository.save(
                new BenefitCondition(fieldName, "==", value.toString(), required, benefit)
        );
    }

    // ===== 공통 =====

    public List<BenefitCondition> findAll() {
        return benefitConditionRepository.findAll();
    }

    public void delete(Long id) {
        benefitConditionRepository.deleteById(id);
    }

    private Benefit findBenefit(Long benefitId) {
        return benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));
    }
}
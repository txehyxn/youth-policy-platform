package com.taehyun.youthpolicyplatform.benefit.service;

import com.taehyun.youthpolicyplatform.benefit.domain.Benefit;
import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCategory;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitCategoryRepository;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// 정책 관련 비즈니스 로직을 처리하는 서비스
@Service
@RequiredArgsConstructor
public class BenefitService {

    private final BenefitRepository benefitRepository;
    private final BenefitCategoryRepository benefitCategoryRepository;

    // 정책 등록
    public Benefit save(
            String title,
            String description,
            String supportAmount,
            String applicationUrl,
            Long categoryId
    ) {
        BenefitCategory category = benefitCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        Benefit benefit = new Benefit(
                title,
                description,
                supportAmount,
                applicationUrl,
                category
        );

        return benefitRepository.save(benefit);
    }

    // 전체 정책 조회
    public List<Benefit> findAll() {
        return benefitRepository.findAll();
    }
}
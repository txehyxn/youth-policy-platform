package com.taehyun.youthpolicyplatform.benefit.service;

import com.taehyun.youthpolicyplatform.benefit.domain.BenefitCategory;
import com.taehyun.youthpolicyplatform.benefit.repository.BenefitCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenefitCategoryService {

    private final BenefitCategoryRepository benefitCategoryRepository;

    // 카테고리 등록
    public BenefitCategory save(String name) {
        BenefitCategory category = new BenefitCategory(name);
        return benefitCategoryRepository.save(category);
    }

    // 전체 카테고리 조회
    public List<BenefitCategory> findAll() {
        return benefitCategoryRepository.findAll();
    }
}
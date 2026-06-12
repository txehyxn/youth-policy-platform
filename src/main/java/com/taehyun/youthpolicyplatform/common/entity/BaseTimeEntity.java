package com.taehyun.youthpolicyplatform.common.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 모든 엔티티에서 공통으로 사용하는 생성일, 수정일 관리 클래스
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    // 데이터 생성 시간
    @CreatedDate
    private LocalDateTime createdAt;

    // 데이터 수정 시간
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
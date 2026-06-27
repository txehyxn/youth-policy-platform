package com.taehyun.youthpolicyplatform.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConditionDisplayDto {

    private String fieldLabel;
    private String operatorLabel;
    private String valueLabel;
}
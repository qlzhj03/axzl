package com.axzl.biz.domain.dto;

/**
 * 满意度评价请求DTO
 *
 * @author liuchao
 */
public class SatisfactionRequest {
    
    /** 满意度评分(1-5) */
    private Integer satisfaction;

    public Integer getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Integer satisfaction) {
        this.satisfaction = satisfaction;
    }
}

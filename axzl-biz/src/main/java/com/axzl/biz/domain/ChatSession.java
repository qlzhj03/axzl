package com.axzl.biz.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 问诊会话表 biz_chat_session
 *
 * @author liuchao
 */
@TableName("biz_chat_session")
public class ChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID(UUID) */
    @TableId
    private String sessionId;

    /** 用户ID */
    private Long userId;

    /** 用户昵称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String nickName;

    /** 会话标题 */
    private String title;

    /** 症状摘要 */
    private String symptomSummary;

    /** AI诊断摘要 */
    private AiDiagnosisContent aiDiagnosis;

    /** 推荐科室标签 */
    private String recommendedLabel;

    /** 消息数量 */
    private Integer messageCount;

    /** 满意度(1-5星) */
    private Integer satisfaction;

    /** 消耗Token数 */
    private Integer tokensUsed;

    /** 最后消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastMessageTime;

    /** 状态(0进行中 1已结束) */
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

    /** 备注 */
    private String remark;

    /** 是否置顶(0否 1是) */
    private Integer isPinned;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSymptomSummary() {
        return symptomSummary;
    }

    public void setSymptomSummary(String symptomSummary) {
        this.symptomSummary = symptomSummary;
    }

    public AiDiagnosisContent getAiDiagnosis() {
        return aiDiagnosis;
    }

    public void setAiDiagnosis(AiDiagnosisContent aiDiagnosis) {
        this.aiDiagnosis = aiDiagnosis;
    }

    public String getRecommendedLabel() {
        return recommendedLabel;
    }

    public void setRecommendedLabel(String recommendedLabel) {
        this.recommendedLabel = recommendedLabel;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public Integer getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Integer satisfaction) {
        this.satisfaction = satisfaction;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    /**
     * AI诊断内容（用于JSON序列化存储在text字段）
     */
    public static class AiDiagnosisContent implements Serializable {
        private static final long serialVersionUID = 1L;
        private String summary;
        private String disease;
        private String recommendation;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getDisease() {
            return disease;
        }

        public void setDisease(String disease) {
            this.disease = disease;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }
}

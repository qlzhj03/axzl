package com.axzl.biz.consultation.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 对话消息表 biz_chat_message
 *
 * @author liuchao
 */
@TableName("biz_chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    @TableId(type = IdType.AUTO)
    private Long messageId;

    /** 会话ID */
    private String sessionId;

    /** 用户ID */
    private Long userId;

    /** 角色(user/assistant) */
    private String role;

    /** 消息内容 */
    private String content;

    /** 向量检索结果(Top5) - JSON字符串 */
    private String retrievalResults;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRetrievalResults() {
        return retrievalResults;
    }

    public void setRetrievalResults(String retrievalResults) {
        this.retrievalResults = retrievalResults;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

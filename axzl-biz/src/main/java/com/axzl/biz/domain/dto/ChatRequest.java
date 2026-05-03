package com.axzl.biz.domain.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 智能问诊请求DTO
 *
 * @author liuchao
 */
public class ChatRequest {
    
    /** 会话ID */
    private String sessionId;
    
    /** 消息内容 */
    @NotNull(message = "消息内容不能为空")
    private String content;

    /** 是否启用知识库增强（默认true） */
    private boolean useKnowledgeBase = true;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUseKnowledgeBase() {
        return useKnowledgeBase;
    }

    public void setUseKnowledgeBase(boolean useKnowledgeBase) {
        this.useKnowledgeBase = useKnowledgeBase;
    }
}

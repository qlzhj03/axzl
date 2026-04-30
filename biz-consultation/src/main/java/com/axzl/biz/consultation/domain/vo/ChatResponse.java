package com.axzl.biz.consultation.domain.vo;

/**
 * 智能问诊响应VO
 *
 * @author liuchao
 */
public class ChatResponse {
    
    /** 会话ID */
    private String sessionId;
    
    /** 角色(user/assistant) */
    private String role;
    
    /** 消息内容 */
    private String content;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
}

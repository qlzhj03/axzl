package com.axzl.biz.domain.vo;

import com.axzl.biz.domain.ChatMessage;
import com.axzl.biz.domain.ChatSession;

import java.util.List;

/**
 * 会话详情VO（包含会话信息和消息列表）
 *
 * @author liuchao
 */
public class ChatSessionDetail {
    
    /** 会话信息 */
    private ChatSession session;
    
    /** 消息列表 */
    private List<ChatMessage> messages;

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}

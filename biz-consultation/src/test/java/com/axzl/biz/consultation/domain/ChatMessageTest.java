package com.axzl.biz.consultation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对话消息实体单元测试
 *
 * @author liuchao
 */
class ChatMessageTest {

    @Test
    @DisplayName("ChatMessage - 所有字段getter/setter正常")
    void testMessageFieldAccess() {
        ChatMessage message = new ChatMessage();
        Date now = new Date();

        message.setMessageId(1L);
        message.setSessionId("uuid-123");
        message.setUserId(100L);
        message.setRole("user");
        message.setContent("我头痛三天了");
        message.setRetrievalResults("[{\"content\":\"头痛常见原因\"}]");
        message.setCreateTime(now);

        assertAll("ChatMessage字段验证",
                () -> assertEquals(1L, message.getMessageId()),
                () -> assertEquals("uuid-123", message.getSessionId()),
                () -> assertEquals(100L, message.getUserId()),
                () -> assertEquals("user", message.getRole()),
                () -> assertEquals("我头痛三天了", message.getContent()),
                () -> assertEquals("[{\"content\":\"头痛常见原因\"}]", message.getRetrievalResults()),
                () -> assertEquals(now, message.getCreateTime())
        );
    }

    @Test
    @DisplayName("ChatMessage - 角色枚举值测试")
    void testMessageRoles() {
        ChatMessage userMsg = new ChatMessage();
        userMsg.setRole("user");
        assertEquals("user", userMsg.getRole());

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setRole("assistant");
        assertEquals("assistant", assistantMsg.getRole());
    }

    @Test
    @DisplayName("ChatMessage - 检索结果存储JSON")
    void testRetrievalResultsJson() {
        ChatMessage message = new ChatMessage();
        String jsonResults = "[{\"content\":\"头痛原因\",\"score\":0.95,\"metadata\":{\"label\":\"神经内科\"}}]";

        message.setRetrievalResults(jsonResults);

        assertNotNull(message.getRetrievalResults());
        assertTrue(message.getRetrievalResults().contains("神经内科"));
        assertTrue(message.getRetrievalResults().contains("0.95"));
    }

    @Test
    @DisplayName("ChatMessage - 初始化空值验证")
    void testMessageDefaults() {
        ChatMessage message = new ChatMessage();

        assertNull(message.getMessageId());
        assertNull(message.getSessionId());
        assertNull(message.getRole());
        assertNull(message.getContent());
        assertNull(message.getRetrievalResults());
    }
}

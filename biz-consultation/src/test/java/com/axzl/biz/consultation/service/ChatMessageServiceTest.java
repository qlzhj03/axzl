package com.axzl.biz.consultation.service;

import com.axzl.biz.consultation.domain.ChatMessage;
import com.axzl.biz.consultation.mapper.ChatMessageMapper;
import com.axzl.biz.consultation.service.impl.ChatMessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ChatMessageService 单元测试
 * 测试对话消息服务的查询和插入逻辑
 *
 * @author liuchao
 */
class ChatMessageServiceTest {

    private ChatMessageMapper messageMapper;
    private ChatMessageServiceImpl messageService;

    @BeforeEach
    void setUp() throws Exception {
        messageMapper = mock(ChatMessageMapper.class);
        messageService = new ChatMessageServiceImpl();
        Class<?> superClass = messageService.getClass().getSuperclass();
        Field baseMapperField = null;
        while (superClass != null) {
            try {
                baseMapperField = superClass.getDeclaredField("baseMapper");
                break;
            } catch (NoSuchFieldException e) {
                superClass = superClass.getSuperclass();
            }
        }
        if (baseMapperField != null) {
            baseMapperField.setAccessible(true);
            baseMapperField.set(messageService, messageMapper);
        }
    }

    @Test
    @DisplayName("selectMessagesBySessionId - 按会话查询消息")
    void testSelectMessagesBySessionId() {
        List<ChatMessage> expected = List.of(
                createMessage("msg1", "user", "你好"),
                createMessage("msg2", "assistant", "你好，有什么可以帮助你的？")
        );
        when(messageMapper.selectList(any())).thenReturn(expected);

        List<ChatMessage> result = messageService.selectMessagesBySessionId("session-123");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageMapper).selectList(any());
    }

    @Test
    @DisplayName("selectMessagesBySessionId - 空会话返回空列表")
    void testSelectMessagesBySessionIdEmpty() {
        when(messageMapper.selectList(any())).thenReturn(List.of());

        List<ChatMessage> result = messageService.selectMessagesBySessionId("empty-session");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("insertMessage - createTime为null时自动设置")
    void testInsertMessageAutoSetCreateTime() {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId("session-1");
        msg.setRole("user");
        msg.setContent("测试消息");
        assertNull(msg.getCreateTime());

        when(messageMapper.insert(any(ChatMessage.class))).thenReturn(1);

        int result = messageService.insertMessage(msg);

        assertEquals(1, result);
        assertNotNull(msg.getCreateTime());
        verify(messageMapper).insert(any(ChatMessage.class));
    }

    @Test
    @DisplayName("insertMessage - createTime已存在时保持不变")
    void testInsertMessagePreserveCreateTime() {
        Date existingDate = new Date(1000000L);
        ChatMessage msg = new ChatMessage();
        msg.setSessionId("session-1");
        msg.setRole("user");
        msg.setContent("测试消息");
        msg.setCreateTime(existingDate);

        when(messageMapper.insert(any(ChatMessage.class))).thenReturn(1);

        int result = messageService.insertMessage(msg);

        assertEquals(1, result);
        assertEquals(existingDate, msg.getCreateTime());
    }

    @Test
    @DisplayName("selectRecentMessages - 查询最近N条消息")
    void testSelectRecentMessages() {
        List<ChatMessage> expected = List.of(
                createMessage("msg3", "user", "最新消息")
        );
        when(messageMapper.selectList(any())).thenReturn(expected);

        List<ChatMessage> result = messageService.selectRecentMessages("session-123", 5);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(messageMapper).selectList(any());
    }

    @Test
    @DisplayName("selectRecentMessages - limit为1时只返回一条")
    void testSelectRecentMessagesLimitOne() {
        List<ChatMessage> expected = List.of(
                createMessage("msg5", "assistant", "最后一条")
        );
        when(messageMapper.selectList(any())).thenReturn(expected);

        List<ChatMessage> result = messageService.selectRecentMessages("session-123", 1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("selectRecentMessages - limit为0时返回空")
    void testSelectRecentMessagesLimitZero() {
        when(messageMapper.selectList(any())).thenReturn(List.of());

        List<ChatMessage> result = messageService.selectRecentMessages("session-123", 0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private ChatMessage createMessage(String msgId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId("session-123");
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(new Date());
        return msg;
    }
}

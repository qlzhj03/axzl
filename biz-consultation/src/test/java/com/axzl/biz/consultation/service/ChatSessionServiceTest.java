package com.axzl.biz.consultation.service;

import com.axzl.biz.consultation.domain.ChatSession;
import com.axzl.biz.consultation.mapper.ChatSessionMapper;
import com.axzl.biz.consultation.service.impl.ChatSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ChatSession服务层单元测试
 *
 * @author liuchao
 */
@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    private ChatSessionMapper sessionMapper;

    private ChatSessionServiceImpl sessionService;

    @BeforeEach
    void setUp() throws Exception {
        sessionService = new ChatSessionServiceImpl();
        // 通过反射向父类设置 baseMapper
        Class<?> superClass = sessionService.getClass().getSuperclass();
        java.lang.reflect.Field baseMapperField = null;
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
            baseMapperField.set(sessionService, sessionMapper);
        }
    }

    @Test
    @DisplayName("新增会话 - createTime为null时应自动设置")
    void testInsertSessionWithNullCreateTime() {
        when(sessionMapper.insert(any(ChatSession.class))).thenReturn(1);

        ChatSession session = new ChatSession();
        session.setSessionId("test-uuid");
        session.setUserId(100L);
        session.setTitle("测试会话");

        int result = sessionService.insertSession(session);

        assertEquals(1, result);
        assertNotNull(session.getUpdateTime());
        assertNotNull(session.getCreateTime());
    }

    @Test
    @DisplayName("新增会话 - createTime已设置时保持不变")
    void testInsertSessionWithExistingCreateTime() {
        when(sessionMapper.insert(any(ChatSession.class))).thenReturn(1);

        Date existingDate = new Date(1000000L);
        ChatSession session = new ChatSession();
        session.setSessionId("test-uuid-2");
        session.setUserId(200L);
        session.setCreateTime(existingDate);

        int result = sessionService.insertSession(session);

        assertEquals(1, result);
        assertEquals(existingDate, session.getCreateTime());
        assertNotNull(session.getUpdateTime());
    }

    @Test
    @DisplayName("更新会话 - 应设置新的updateTime")
    void testUpdateSession() {
        when(sessionMapper.updateById(any(ChatSession.class))).thenReturn(1);

        ChatSession session = new ChatSession();
        session.setSessionId("test-uuid");
        session.setTitle("更新标题");

        int result = sessionService.updateSession(session);

        assertEquals(1, result);
        assertNotNull(session.getUpdateTime());
    }

    @Test
    @DisplayName("删除会话 - 批量删除成功部分")
    void testDeleteSessionByIds() {
        when(sessionMapper.deleteById("id1")).thenReturn(1);
        when(sessionMapper.deleteById("id2")).thenReturn(1);
        when(sessionMapper.deleteById("id3")).thenReturn(0);

        int result = sessionService.deleteSessionByIds(new String[]{"id1", "id2", "id3"});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("删除会话 - 全部删除成功")
    void testDeleteSessionByIdsAllSuccess() {
        when(sessionMapper.deleteById("id1")).thenReturn(1);
        when(sessionMapper.deleteById("id2")).thenReturn(1);

        int result = sessionService.deleteSessionByIds(new String[]{"id1", "id2"});

        assertEquals(2, result);
    }

    @Test
    @DisplayName("更新满意度 - 应同时更新updateTime")
    @org.junit.jupiter.api.Disabled("LambdaUpdateWrapper需要Spring上下文初始化缓存")
    void testUpdateSatisfaction() {
        when(sessionMapper.update(any())).thenReturn(1);

        int result = sessionService.updateSatisfaction("session-123", 5);

        assertEquals(1, result);
        verify(sessionMapper).update(any());
    }

    @Test
    @DisplayName("查询会话列表 - 按userId过滤")
    void testSelectSessionListByUserId() {
        ChatSession query = new ChatSession();
        query.setUserId(100L);
        when(sessionMapper.selectList(any())).thenReturn(List.of());

        sessionService.selectSessionList(query);

        verify(sessionMapper).selectList(any());
    }

    @Test
    @DisplayName("查询会话列表 - 按title模糊查询")
    void testSelectSessionListByTitle() {
        ChatSession query = new ChatSession();
        query.setTitle("头痛");
        when(sessionMapper.selectList(any())).thenReturn(List.of());

        sessionService.selectSessionList(query);

        verify(sessionMapper).selectList(any());
    }

    @Test
    @DisplayName("查询会话列表 - 按status查询")
    void testSelectSessionListByStatus() {
        ChatSession query = new ChatSession();
        query.setStatus("0");
        when(sessionMapper.selectList(any())).thenReturn(List.of());

        sessionService.selectSessionList(query);

        verify(sessionMapper).selectList(any());
    }

    @Test
    @DisplayName("根据ID查询会话 - 成功")
    void testSelectSessionByIdSuccess() {
        ChatSession session = new ChatSession();
        session.setSessionId("test-uuid");
        session.setTitle("测试");
        when(sessionMapper.selectById("test-uuid")).thenReturn(session);

        ChatSession result = sessionService.selectSessionById("test-uuid");

        assertNotNull(result);
        assertEquals("测试", result.getTitle());
    }

    @Test
    @DisplayName("根据ID查询会话 - 未找到")
    void testSelectSessionByIdNotFound() {
        when(sessionMapper.selectById("not-exist")).thenReturn(null);

        ChatSession result = sessionService.selectSessionById("not-exist");

        assertNull(result);
    }
}

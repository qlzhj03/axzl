package com.axzl.biz.consultation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 问诊会话实体单元测试
 *
 * @author liuchao
 */
class ChatSessionTest {

    @Test
    @DisplayName("ChatSession - 所有字段getter/setter正常")
    void testSessionFieldAccess() {
        ChatSession session = new ChatSession();
        Date now = new Date();

        session.setSessionId("uuid-123");
        session.setUserId(100L);
        session.setTitle("头痛问诊");
        session.setSymptomSummary("持续三天头痛");
        session.setRecommendedLabel("神经内科");
        session.setMessageCount(10);
        session.setSatisfaction(4);
        session.setTokensUsed(500);
        session.setLastMessageTime(now);
        session.setStatus("0");
        session.setCreateTime(now);
        session.setUpdateTime(now);
        session.setRemark("备注");

        assertAll("ChatSession字段验证",
                () -> assertEquals("uuid-123", session.getSessionId()),
                () -> assertEquals(100L, session.getUserId()),
                () -> assertEquals("头痛问诊", session.getTitle()),
                () -> assertEquals("持续三天头痛", session.getSymptomSummary()),
                () -> assertEquals("神经内科", session.getRecommendedLabel()),
                () -> assertEquals(10, session.getMessageCount()),
                () -> assertEquals(4, session.getSatisfaction()),
                () -> assertEquals(500, session.getTokensUsed()),
                () -> assertEquals(now, session.getLastMessageTime()),
                () -> assertEquals("0", session.getStatus()),
                () -> assertEquals(now, session.getCreateTime()),
                () -> assertEquals("备注", session.getRemark())
        );
    }

    @Test
    @DisplayName("ChatSession.AiDiagnosisContent - 字段getter/setter正常")
    void testAiDiagnosisContent() {
        ChatSession.AiDiagnosisContent content = new ChatSession.AiDiagnosisContent();

        content.setSummary("可能是紧张性头痛");
        content.setDisease("紧张性头痛");
        content.setRecommendation("建议休息，必要时就医检查");

        assertAll("AiDiagnosisContent字段验证",
                () -> assertEquals("可能是紧张性头痛", content.getSummary()),
                () -> assertEquals("紧张性头痛", content.getDisease()),
                () -> assertEquals("建议休息，必要时就医检查", content.getRecommendation())
        );
    }

    @Test
    @DisplayName("ChatSession - AiDiagnosis嵌套对象")
    void testSessionWithAiDiagnosis() {
        ChatSession session = new ChatSession();
        ChatSession.AiDiagnosisContent diagnosis = new ChatSession.AiDiagnosisContent();
        diagnosis.setSummary("诊断摘要");
        diagnosis.setDisease("偏头痛");
        diagnosis.setRecommendation("建议神经内科就诊");

        session.setAiDiagnosis(diagnosis);

        assertNotNull(session.getAiDiagnosis());
        assertEquals("偏头痛", session.getAiDiagnosis().getDisease());
    }

    @Test
    @DisplayName("ChatSession - 初始化空值验证")
    void testSessionDefaults() {
        ChatSession session = new ChatSession();

        assertNull(session.getSessionId());
        assertNull(session.getMessageCount());
        assertNull(session.getSatisfaction());
        assertNull(session.getTokensUsed());
        assertNull(session.getStatus());
    }
}

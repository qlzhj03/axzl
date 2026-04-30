package com.axzl.biz.ai.service;

import com.axzl.biz.consultation.domain.ChatMessage;
import com.axzl.biz.consultation.service.IChatMessageService;
import com.axzl.biz.consultation.service.IChatSessionService;
import com.axzl.biz.knowledge.domain.BizKnowledgeMeta;
import com.axzl.biz.knowledge.service.IBizKnowledgeMetaService;
import com.axzl.biz.profile.domain.UserProfile;
import com.axzl.biz.profile.service.IUserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AiConsultationService 单元测试
 * 测试Prompt构建、用户档案上下文、症状提取等纯逻辑
 *
 * @author liuchao
 */
@ExtendWith(MockitoExtension.class)
class AiConsultationServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private IChatSessionService sessionService;

    @Mock
    private IChatMessageService messageService;

    @Mock
    private IBizKnowledgeMetaService knowledgeMetaService;

    @Mock
    private IUserProfileService profileService;

    @InjectMocks
    private AiConsultationService service;

    @BeforeEach
    void setUp() {
        // 可选：初始化 mock 行为
    }

    // ==================== buildProfileContext 测试 ====================

    @Test
    @DisplayName("档案为null - 应返回默认提示")
    void testBuildProfileContext_Null() throws Exception {
        String result = invokeBuildProfileContext(null);

        assertEquals("暂无用户健康档案信息。", result);
    }

    @Test
    @DisplayName("完整档案 - 应包含所有信息")
    void testBuildProfileContext_Full() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setGender("0"); // 男
        profile.setBirthday(new Date());
        profile.setBloodType("A");
        profile.setAllergyHistory("青霉素过敏");
        profile.setMedicalHistory("高血压、糖尿病");

        String result = invokeBuildProfileContext(profile);

        assertTrue(result.contains("用户健康档案"));
        assertTrue(result.contains("男"));
        assertTrue(result.contains("血型：A"));
        assertTrue(result.contains("青霉素过敏"));
        assertTrue(result.contains("高血压、糖尿病"));
    }

    @Test
    @DisplayName("部分档案 - 应只包含有值的字段")
    void testBuildProfileContext_Partial() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setGender("1"); // 女
        // 其他字段为null

        String result = invokeBuildProfileContext(profile);

        assertTrue(result.contains("用户健康档案"));
        assertTrue(result.contains("女"));
        assertFalse(result.contains("血型"));
        assertFalse(result.contains("过敏史"));
    }

    // ==================== extractSymptomSummary 测试 ====================

    @Test
    @DisplayName("短症状描述 - 应完整返回")
    void testExtractSymptomSummary_Short() throws Exception {
        String msg = "头痛";

        String result = invokeExtractSymptomSummary(msg);

        assertEquals("头痛", result);
    }

    @Test
    @DisplayName("超长症状描述 - 应截取前200字符")
    void testExtractSymptomSummary_Long() throws Exception {
        String msg = "A".repeat(500);

        String result = invokeExtractSymptomSummary(msg);

        assertEquals(200, result.length());
        assertTrue(msg.startsWith(result));
    }

    // ==================== extractRecommendedLabel 测试 ====================

    @Test
    @DisplayName("无检索结果 - 应返回全科")
    void testExtractRecommendedLabel_Empty() throws Exception {
        String result = invokeExtractRecommendedLabel(Collections.emptyList());

        assertEquals("全科", result);
    }

    @Test
    @DisplayName("有标签检索结果 - 应返回第一个标签")
    void testExtractRecommendedLabel_WithLabel() throws Exception {
        Document doc = new Document("测试内容",
                Map.of("label", "心血管内科"));

        String result = invokeExtractRecommendedLabel(List.of(doc));

        assertEquals("心血管内科", result);
    }

    @Test
    @DisplayName("检索结果无标签 - 应返回全科")
    void testExtractRecommendedLabel_NoLabel() throws Exception {
        Document doc = new Document("测试内容", Map.of());

        String result = invokeExtractRecommendedLabel(List.of(doc));

        assertEquals("全科", result);
    }

    // ==================== estimateTokens 测试 ====================

    @Test
    @DisplayName("估算Token数 - null应返回0")
    void testEstimateTokens_Null() throws Exception {
        int result = invokeEstimateTokens(null);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("估算Token数 - 中文文本")
    void testEstimateTokens_Chinese() throws Exception {
        String text = "这是一段中文测试文本";

        int result = invokeEstimateTokens(text);

        assertTrue(result > 0);
        // 10个中文字符 * 0.8 = 8
        assertEquals(8, result);
    }

    // ==================== buildPrompt 测试 ====================

    @Test
    @DisplayName("构建Prompt - 应包含知识和档案")
    void testBuildPrompt_WithKnowledge() throws Exception {
        String userMessage = "我头痛三天了";
        List<Document> retrievalResults = List.of(
                new Document("头痛常见原因包括紧张性头痛和偏头痛",
                        Map.of("label", "神经内科"))
        );
        List<ChatMessage> history = new ArrayList<>();
        String profileContext = "用户健康档案：\n- 性别：男\n";

        String result = invokeBuildPrompt(userMessage, retrievalResults, history, profileContext);

        assertTrue(result.contains("【参考知识】"));
        assertTrue(result.contains("头痛常见原因"));
        assertTrue(result.contains("用户健康档案"));
        assertTrue(result.contains("用户当前症状描述"));
        assertTrue(result.contains("我头痛三天了"));
    }

    @Test
    @DisplayName("构建Prompt - 无知识时应有提示")
    void testBuildPrompt_NoKnowledge() throws Exception {
        String result = invokeBuildPrompt(
                "肚子疼",
                Collections.emptyList(),
                new ArrayList<>(),
                "暂无用户健康档案信息。"
        );

        assertTrue(result.contains("暂无相关医学知识匹配"));
        assertTrue(result.contains("肚子疼"));
    }

    // ==================== 反射辅助方法 ====================

    private String invokeBuildProfileContext(UserProfile profile) throws Exception {
        Method method = AiConsultationService.class.getDeclaredMethod(
                "buildProfileContext", UserProfile.class);
        method.setAccessible(true);
        return (String) method.invoke(service, profile);
    }

    private String invokeExtractSymptomSummary(String msg) throws Exception {
        Method method = AiConsultationService.class.getDeclaredMethod(
                "extractSymptomSummary", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, msg);
    }

    @SuppressWarnings("unchecked")
    private String invokeExtractRecommendedLabel(List<Document> docs) throws Exception {
        Method method = AiConsultationService.class.getDeclaredMethod(
                "extractRecommendedLabel", List.class);
        method.setAccessible(true);
        return (String) method.invoke(service, docs);
    }

    private int invokeEstimateTokens(String text) throws Exception {
        Method method = AiConsultationService.class.getDeclaredMethod(
                "estimateTokens", String.class);
        method.setAccessible(true);
        return (int) method.invoke(service, text);
    }

    @SuppressWarnings("unchecked")
    private String invokeBuildPrompt(String userMessage, List<Document> results,
                                      List<ChatMessage> history, String profileCtx) throws Exception {
        Method method = AiConsultationService.class.getDeclaredMethod(
                "buildPrompt", String.class, List.class, List.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, userMessage, results, history, profileCtx);
    }
}

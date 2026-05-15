package com.axzl.biz.service.impl;

import com.alibaba.fastjson2.JSON;
import com.axzl.biz.domain.ChatMessage;
import com.axzl.biz.domain.ChatSession;
import com.axzl.biz.domain.UserProfile;
import com.axzl.biz.service.IAiConsultationService;
import com.axzl.biz.service.IBizKnowledgeMetaService;
import com.axzl.biz.service.IChatMessageService;
import com.axzl.biz.service.IChatSessionService;
import com.axzl.biz.service.IUserProfileService;
import com.axzl.biz.service.PromptTemplateLoader;
import com.axzl.biz.service.SpringAIClientFactory;
import com.axzl.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * AI智能问诊服务实现类
 * 基于 Spring AI 实现完整 RAG 流程：
 * 用户输入 → Embedding → VectorStore检索 → Prompt构建 → ChatClient生成 → 保存 → 返回
 * 
 * 使用SpringAIClientFactory统一管理AI客户端，支持动态切换模型
 *
 * @author liuchao
 */
@Service
public class AiConsultationServiceImpl implements IAiConsultationService {

    private static final Logger log = LoggerFactory.getLogger(AiConsultationServiceImpl.class);

    @Autowired
    private SpringAIClientFactory clientFactory;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private IChatSessionService sessionService;

    @Autowired
    private IChatMessageService messageService;

    @Autowired
    private IBizKnowledgeMetaService knowledgeMetaService;

    @Autowired
    private IUserProfileService profileService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private PromptTemplateLoader promptTemplateLoader;

    /**
     * 智能问诊对话（完整RAG流程）
     *
     * @param sessionId   会话ID（空则创建新会话）
     * @param userMessage 用户消息内容
     * @return AI回复
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String chat(Long userId,String sessionId, String userMessage, boolean useKnowledgeBase) {

        log.info("AI问诊 - 用户: {}, 会话: {}, 消息: {}, 知识库增强: {}", userId, sessionId, userMessage, useKnowledgeBase);

        // 0. 安全检测 - 越狱/恶意提问拒答
        if (promptTemplateLoader.containsUnsafeRequest(userMessage)) {
            log.warn("检测到非医疗相关提问，返回拒答消息 - 用户: {}", userId);
            return promptTemplateLoader.getRefusalMessage();
        }

        // 1. 获取或创建会话
        ChatSession session;
        if (StringUtils.isEmpty(sessionId)) {
            sessionId = UUID.randomUUID().toString();
            session = new ChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setTitle(userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage);
            session.setMessageCount(0);
            session.setTokensUsed(0);
            session.setStatus("0");
            session.setCreateTime(new Date());
            session.setLastMessageTime(new Date());
            sessionService.insertSession(session);
        } else {
            // 获取当前用户的此次会话
            session = sessionService.selectSessionByUserIdAndSessionId(userId, sessionId);
            if (session == null) {
                throw new RuntimeException("会话不存在: " + sessionId);
            }
        }

        // 2. 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setUserId(userId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setCreateTime(new Date());
        messageService.insertMessage(userMsg);

        // 3. 向量检索 - 从知识库中检索相关内容（根据开关决定是否启用）
        List<Document> retrievalResults = useKnowledgeBase ? performVectorSearch(userMessage) : Collections.emptyList();
        List<Map<String, Object>> retrievalDetails = new ArrayList<>();
        for (Document doc : retrievalResults) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("content", doc.getText());
            detail.put("metadata", doc.getMetadata());
            detail.put("score", doc.getScore());
            retrievalDetails.add(detail);
        }

        // 4. 获取用户健康档案（作为上下文）
        UserProfile profile = profileService.selectProfileByUserId(userId);
        String profileContext = buildProfileContext(profile);

        // 5. 获取历史消息
        List<ChatMessage> recentMessages = messageService.selectRecentMessages(sessionId, 30);

        // 6. 构建Prompt（含紧急预警检测）
        boolean isEmergency = promptTemplateLoader.containsEmergencyKeywords(userMessage);
        String prompt = buildPrompt(userMessage, retrievalResults, recentMessages, profileContext, useKnowledgeBase, isEmergency);

        // 7. 调用AI生成回复（使用默认对话模型）
        ChatClient currentChatClient = clientFactory.getDefaultChatClient();
        String aiResponse = currentChatClient.prompt()
                .user(prompt)
                .call().content();

        // 7.1 追加医疗免责声明
        aiResponse = aiResponse + PromptTemplateLoader.MEDICAL_DISCLAIMER;

        // 8. 保存AI回复
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setUserId(userId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(aiResponse);
        assistantMsg.setRetrievalResults(JSON.toJSONString(retrievalDetails));
        assistantMsg.setCreateTime(new Date());
        messageService.insertMessage(assistantMsg);

        // 9. 更新会话信息
        session.setMessageCount(session.getMessageCount() != null ? session.getMessageCount() + 2 : 2);
        session.setTokensUsed(session.getTokensUsed() != null ? session.getTokensUsed() + estimateTokens(prompt) + estimateTokens(aiResponse) : 200);
        session.setLastMessageTime(new Date());
        session.setUpdateTime(new Date());

        // 提取症状摘要和推荐科室
        String symptomSummary = extractSymptomSummary(userMessage);
        session.setSymptomSummary(symptomSummary);
        session.setRecommendedLabel(extractRecommendedLabel(retrievalResults));

        sessionService.updateSession(session);

        return aiResponse; // 返回完整响应
    }

    /**
     * 智能问诊对话（流式版本）
     *
     * @param sessionId   会话ID（空则创建新会话）
     * @param userMessage 用户消息内容
     * @return AI回复的Flux流
     */
    @Override
    public Flux<String> chatStream(Long userId,String sessionId, String userMessage, boolean useKnowledgeBase) {
        log.info("AI问诊(流式) - 用户: {}, 会话: {}, 消息: {}, 知识库增强: {}", userId, sessionId, userMessage, useKnowledgeBase);

        // 0. 安全检测 - 越狱/恶意提问拒答
        if (promptTemplateLoader.containsUnsafeRequest(userMessage)) {
            log.warn("检测到非医疗相关提问，返回拒答消息 - 用户: {}", userId);
            return Flux.just(promptTemplateLoader.getRefusalMessage());
        }

        // 1. 获取或创建会话（事务内）
        final String[] sessionIdHolder = {sessionId};
        final boolean[] isNewSession = {false};
        ChatSession session = transactionTemplate.execute(status -> {
            ChatSession sess;
            if (StringUtils.isEmpty(sessionIdHolder[0])) {
                sessionIdHolder[0] = UUID.randomUUID().toString();
                sess = new ChatSession();
                sess.setSessionId(sessionIdHolder[0]);
                sess.setUserId(userId);
                sess.setTitle(userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage);
                sess.setMessageCount(0);
                sess.setTokensUsed(0);
                sess.setStatus("0");
                sess.setCreateTime(new Date());
                sess.setLastMessageTime(new Date());
                sessionService.insertSession(sess);
                isNewSession[0] = true;
            } else {
                sess = sessionService.selectSessionByUserIdAndSessionId(userId, sessionIdHolder[0]);
                if (sess == null) {
                    throw new RuntimeException("会话不存在: " + sessionIdHolder[0]);
                }
            }

            // 2. 保存用户消息
            ChatMessage userMsg = new ChatMessage();
            userMsg.setSessionId(sessionIdHolder[0]);
            userMsg.setUserId(userId);
            userMsg.setRole("user");
            userMsg.setContent(userMessage);
            userMsg.setCreateTime(new Date());
            messageService.insertMessage(userMsg);
            return sess;
        });

        // 3. 向量检索 - 从知识库中检索相关内容（根据开关决定是否启用）
        List<Document> retrievalResults = useKnowledgeBase ? performVectorSearch(userMessage) : Collections.emptyList();
        List<Map<String, Object>> retrievalDetails = new ArrayList<>();
        for (Document doc : retrievalResults) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("content", doc.getText());
            detail.put("metadata", doc.getMetadata());
            detail.put("score", doc.getScore());
            retrievalDetails.add(detail);
        }

        // 4. 获取用户健康档案（作为上下文）
        UserProfile profile = profileService.selectProfileByUserId(userId);
        String profileContext = buildProfileContext(profile);

        // 5. 获取历史消息
        List<ChatMessage> recentMessages = messageService.selectRecentMessages(sessionIdHolder[0], 30);

        // 6. 构建Prompt（含紧急预警检测）
        boolean isEmergency = promptTemplateLoader.containsEmergencyKeywords(userMessage);
        String prompt = buildPrompt(userMessage, retrievalResults, recentMessages, profileContext, useKnowledgeBase, isEmergency);

        // 7. 调用AI生成流式回复（使用默认对话模型）
        Flux<String> aiResponse;
        try {
            ChatClient currentChatClient = clientFactory.getDefaultChatClient();
            aiResponse = currentChatClient.prompt()
                    .user(prompt)
                    .stream().content()
                    .cache(); // 缓存流，使多个订阅者共享同一个AI调用
        } catch (Exception e) {
            log.error("AI调用失败", e);
            // AI调用失败，清理已保存的会话和用户消息
            if (isNewSession[0]) {
                sessionService.deleteSessionByIds(new String[]{sessionIdHolder[0]});
                messageService.remove(new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionIdHolder[0]));
            }
            return Flux.just("抱歉，AI服务暂时不可用，请稍后重试。您可以先描述更多症状信息，我会在服务恢复后为您分析。");
        }

        // 8. 在流结束后异步保存完整的AI回复到数据库
        final String finalSessionId = sessionIdHolder[0];
        final Long finalUserId = userId;
        final List<Map<String, Object>> finalRetrievalDetails = retrievalDetails;
        final ChatSession finalSession = session;
        final String finalPrompt = prompt;
        final String finalUserMessage = userMessage;
        final List<Document> finalRetrievalResults = retrievalResults;

        // 使用 StringBuilder 累积完整响应
        final StringBuilder responseBuilder = new StringBuilder();
        // 标记流是否发生错误
        final boolean[] streamError = {false};

        // 先启动一个后台订阅来累积并保存响应
        // 这个订阅不受前端SSE连接影响
        aiResponse
                .doOnNext(responseBuilder::append)
                .doOnError(error -> streamError[0] = true)
                .doFinally(signalType -> {
                    if (streamError[0]) {
                        // 流发生错误，清理已保存的会话和用户消息
                        log.warn("AI流处理出错，清理会话数据 - 会话: {}", finalSessionId);
                        if (isNewSession[0]) {
                            sessionService.deleteSessionByIds(new String[]{finalSessionId});
                            messageService.remove(new LambdaQueryWrapper<ChatMessage>()
                                    .eq(ChatMessage::getSessionId, finalSessionId));
                        }
                        return;
                    }
                    // 流正常结束，保存已累积的响应（含免责声明）
                    String completeResponse = responseBuilder.toString() + PromptTemplateLoader.MEDICAL_DISCLAIMER;
                    if (!completeResponse.isEmpty()) {
                        saveAiResponseAsync(
                            finalSessionId,
                            finalUserId,
                            completeResponse,
                            finalRetrievalDetails,
                            finalSession,
                            finalPrompt,
                            finalUserMessage,
                            finalRetrievalResults
                        );
                    } else {
                        log.warn("AI响应为空，跳过保存 - 会话: {}", finalSessionId);
                    }
                })
                .subscribe(
                    chunk -> {}, // 仅累积，不做其他处理
                    error -> {
                        String message = error.getMessage();
                        if (message != null && (message.contains("你的主机中的软件中止了一个已建立的连接")
                                || message.contains("Connection reset by peer")
                                || message.contains("Broken pipe")
                                || message.contains("AsyncRequestNotUsableException")
                                || message.contains("ResponseBodyEmitter has already completed"))) {
                            log.debug("AI流处理客户端已断开 - 会话: {}", finalSessionId);
                        } else {
                            log.error("AI流处理出错 - 会话: {}", finalSessionId, error);
                        }
                    }
                );

        // 在 Flux 头部注入 sessionId，尾部追加医疗免责声明
        Flux<String> sessionIdFlux = Flux.just("{\"sessionId\":\"" + finalSessionId + "\"}");
        Flux<String> disclaimerFlux = Flux.just(PromptTemplateLoader.MEDICAL_DISCLAIMER);
        return Flux.concat(sessionIdFlux, aiResponse, disclaimerFlux);
    }

    /**
     * 异步保存AI回复到数据库（提升性能）
     */
    private void saveAiResponseAsync(
            String sessionId,
            Long userId,
            String completeResponse,
            List<Map<String, Object>> retrievalDetails,
            ChatSession session,
            String prompt,
            String userMessage,
            List<Document> retrievalResults
    ) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                // 保存AI助手消息
                ChatMessage assistantMsg = new ChatMessage();
                assistantMsg.setSessionId(sessionId);
                assistantMsg.setUserId(userId);
                assistantMsg.setRole("assistant");
                assistantMsg.setContent(completeResponse);
                assistantMsg.setRetrievalResults(JSON.toJSONString(retrievalDetails));
                assistantMsg.setCreateTime(new Date());
                messageService.insertMessage(assistantMsg);

                // 更新会话信息
                session.setMessageCount(session.getMessageCount() != null ? session.getMessageCount() + 2 : 2);
                session.setTokensUsed(session.getTokensUsed() != null ? session.getTokensUsed() + estimateTokens(prompt) + estimateTokens(completeResponse) : 200);
                session.setLastMessageTime(new Date());
                session.setUpdateTime(new Date());

                // 提取症状摘要和推荐科室
                String symptomSummary = extractSymptomSummary(userMessage);
                session.setSymptomSummary(symptomSummary);
                session.setRecommendedLabel(extractRecommendedLabel(retrievalResults));

                sessionService.updateSession(session);
            });
            log.info("AI回复保存成功 - 会话: {}, 长度: {}", sessionId, completeResponse.length());
        } catch (Exception e) {
            log.error("保存AI回复失败 - 会话: {}", sessionId, e);
        }
    }

    /**
     * 执行向量检索
     */
    private List<Document> performVectorSearch(String query) {
        try {
            // 使用 VectorStore 的相似性搜索
            return vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query)
                            .topK(5)
                            .similarityThreshold(0.6)
                            .build()
            );
        } catch (Exception e) {
            log.warn("向量检索失败，将使用基础回复", e);
            return Collections.emptyList();
        }
    }

    /**
     * 构建用户健康档案上下文
     */
    private String buildProfileContext(UserProfile profile) {
        if (profile == null) {
            return "暂无用户健康档案信息。";
        }
        StringBuilder sb = new StringBuilder("用户健康档案：\n");
        if (profile.getGender() != null) {
            sb.append("- 性别：").append("0".equals(profile.getGender()) ? "男" :
                    "1".equals(profile.getGender()) ? "女" : "未知").append("\n");
        }
        if (profile.getBirthday() != null) {
            sb.append("- 出生日期：").append(profile.getBirthday()).append("\n");
        }
        if (profile.getBloodType() != null && !profile.getBloodType().isEmpty()) {
            sb.append("- 血型：").append(profile.getBloodType()).append("\n");
        }
        if (profile.getAllergyHistory() != null && !profile.getAllergyHistory().isEmpty()) {
            sb.append("- 过敏史：").append(profile.getAllergyHistory()).append("\n");
        }
        if (profile.getMedicalHistory() != null && !profile.getMedicalHistory().isEmpty()) {
            sb.append("- 既往病史：").append(profile.getMedicalHistory()).append("\n");
        }
        sb.append("如果用户未提到与之相关的问题，请忽略以上信息。");
        return sb.toString();
    }

    /**
     * 构建RAG Prompt（含医疗安全约束）
     *
     * @param isEmergency 是否检测到紧急症状关键词
     */
    private String buildPrompt(String userMessage, List<Document> retrievalResults,
                               List<ChatMessage> history, String profileContext,
                               boolean useKnowledgeBase, boolean isEmergency) {
        StringBuilder prompt = new StringBuilder();

        // 紧急预警前缀
        if (isEmergency) {
            prompt.append(PromptTemplateLoader.EMERGENCY_PREFIX);
        }

        if (useKnowledgeBase) {
            prompt.append("【参考知识】\n");
            if (retrievalResults.isEmpty()) {
                prompt.append("直接使用模型基础知识\n");
            } else {
                for (int i = 0; i < retrievalResults.size(); i++) {
                    Document doc = retrievalResults.get(i);
                    prompt.append(i + 1).append(". ").append(doc.getText()).append("\n");
                }
            }
            prompt.append("\n");
        }

        prompt.append("【").append(profileContext).append("】\n");

        // 历史对话
        if (!history.isEmpty()) {
            prompt.append("\n【历史对话】\n");
            for (ChatMessage msg : history) {
                prompt.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
        }

        prompt.append("\n【用户当前症状描述】\n");
        prompt.append(userMessage).append("\n");

        prompt.append("\n").append(promptTemplateLoader.getResponseFormatRules());

        if (useKnowledgeBase) {
            prompt.append("\n请根据以上参考知识、用户健康档案和历史对话回答用户问题。");
        } else {
            prompt.append("\n请根据你的医学知识、用户健康档案和历史对话回答用户问题。");
        }

        return prompt.toString();
    }

    /**
     * 提取症状摘要
     */
    private String extractSymptomSummary(String userMessage) {
        if (userMessage.length() > 200) {
            return userMessage.substring(0, 200);
        }
        return userMessage;
    }

    /**
     * 提取推荐科室标签
     */
    private String extractRecommendedLabel(List<Document> retrievalResults) {
        if (retrievalResults.isEmpty()) {
            return "全科";
        }
        // 从检索结果中提取出现最多的标签
        return retrievalResults.stream()
                .map(doc -> doc.getMetadata().getOrDefault("label", "").toString())
                .filter(label -> !label.isEmpty())
                .findFirst()
                .orElse("全科");
    }

    /**
     * 估算Token数
     */
    private int estimateTokens(String text) {
        if (text == null) return 0;
        // 粗略估算：中文约每字符1.5 token，英文约每字符0.3 token
        return (int) (text.length() * 0.8);
    }
}

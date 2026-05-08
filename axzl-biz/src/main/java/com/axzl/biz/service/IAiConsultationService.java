package com.axzl.biz.service;

import reactor.core.publisher.Flux;

/**
 * AI智能问诊服务接口
 * 基于 Spring AI 实现完整 RAG 流程：
 * 用户输入 → Embedding → VectorStore检索 → Prompt构建 → ChatClient生成 → 保存 → 返回
 * 
 * 使用SpringAIClientFactory统一管理AI客户端，支持动态切换模型
 *
 * @author liuchao
 */
public interface IAiConsultationService {

    /**
     * 智能问诊对话（完整RAG流程）
     *
     * @param userId 用户ID
     * @param sessionId 会话ID（空则创建新会话）
     * @param userMessage 用户消息内容
     * @param useKnowledgeBase 是否使用知识库增强
     * @return AI回复
     */
    String chat(Long userId, String sessionId, String userMessage, boolean useKnowledgeBase);

    /**
     * 智能问诊对话（流式版本）
     *
     * @param userId 用户ID
     * @param sessionId 会话ID（空则创建新会话）
     * @param userMessage 用户消息内容
     * @param useKnowledgeBase 是否使用知识库增强
     * @return AI回复的Flux流
     */
    Flux<String> chatStream(Long userId, String sessionId, String userMessage, boolean useKnowledgeBase);
}

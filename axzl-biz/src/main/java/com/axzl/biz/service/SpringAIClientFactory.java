package com.axzl.biz.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * Spring AI客户端工厂服务
 * 根据配置的模型动态创建ChatClient和EmbeddingModel
 * 
 * @author liuchao
 */
public interface SpringAIClientFactory {

    /**
     * 获取默认的对话客户端
     * 
     * @return ChatClient
     */
    ChatClient getDefaultChatClient();

    /**
     * 根据模型ID获取对话客户端
     * 
     * @param modelId 模型ID
     * @return ChatClient
     */
    ChatClient getChatClientByModelId(Long modelId);

    /**
     * 根据模型编码获取对话客户端
     * 
     * @param modelCode 模型编码
     * @return ChatClient
     */
    ChatClient getChatClientByModelCode(String modelCode);

    /**
     * 获取默认的Embedding模型
     * 
     * @return EmbeddingModel
     */
    EmbeddingModel getDefaultEmbeddingModel();

    /**
     * 根据模型ID获取Embedding模型
     * 
     * @param modelId 模型ID
     * @return EmbeddingModel
     */
    EmbeddingModel getEmbeddingModelByModelId(Long modelId);

    /**
     * 根据模型编码获取Embedding模型
     * 
     * @param modelCode 模型编码
     * @return EmbeddingModel
     */
    EmbeddingModel getEmbeddingModelByModelCode(String modelCode);

    /**
     * 刷新客户端缓存
     * 当模型配置更新时调用
     */
    void refreshCache();
}

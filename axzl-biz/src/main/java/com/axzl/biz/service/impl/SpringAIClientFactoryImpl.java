package com.axzl.biz.service.impl;

import com.axzl.biz.service.SpringAIClientFactory;
import com.axzl.biz.domain.AIModelConfig;
import com.axzl.biz.service.IAIModelConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring AI客户端工厂实现
 * 根据数据库配置动态创建和管理AI客户端
 * 
 * @author liuchao
 */
@Service
public class SpringAIClientFactoryImpl implements SpringAIClientFactory {
    private static final Logger log = LoggerFactory.getLogger(SpringAIClientFactoryImpl.class);
    private final IAIModelConfigService modelConfigService;
    
    @Autowired
    private ChatClient.Builder chatClientBuilder;

    /**
     * ChatClient缓存
     * Key: modelId
     * Value: ChatClient实例
     */
    private final Map<Long, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    /**
     * EmbeddingModel缓存
     * Key: modelId
     * Value: EmbeddingModel实例
     */
    private final Map<Long, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();

    public SpringAIClientFactoryImpl(IAIModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    /**
     * 获取默认的对话客户端
     */
    @Override
    public ChatClient getDefaultChatClient() {
        AIModelConfig config = modelConfigService.selectDefaultModelByType("text");
        if (config == null) {
            throw new RuntimeException("未配置默认的对话模型");
        }
        return getChatClientByModelId(config.getId());
    }

    /**
     * 根据模型ID获取对话客户端
     */
    @Override
    public ChatClient getChatClientByModelId(Long modelId) {
        return chatClientCache.computeIfAbsent(modelId, this::createChatClient);
    }

    /**
     * 根据模型编码获取对话客户端
     */
    @Override
    public ChatClient getChatClientByModelCode(String modelCode) {
        AIModelConfig config = modelConfigService.selectByModelName(modelCode);
        if (config == null) {
            throw new RuntimeException("模型配置不存在: " + modelCode);
        }
        return getChatClientByModelId(config.getId());
    }

    /**
     * 获取默认的Embedding模型
     */
    @Override
    public EmbeddingModel getDefaultEmbeddingModel() {
        AIModelConfig config = modelConfigService.selectDefaultModelByType("embedding");
        if (config == null) {
            throw new RuntimeException("未配置默认的Embedding模型");
        }
        return getEmbeddingModelByModelId(config.getId());
    }

    /**
     * 根据模型ID获取Embedding模型
     */
    @Override
    public EmbeddingModel getEmbeddingModelByModelId(Long modelId) {
        return embeddingModelCache.computeIfAbsent(modelId, this::createEmbeddingModel);
    }

    /**
     * 根据模型编码获取Embedding模型
     */
    @Override
    public EmbeddingModel getEmbeddingModelByModelCode(String modelCode) {
        AIModelConfig config = modelConfigService.selectByModelName(modelCode);
        if (config == null) {
            throw new RuntimeException("模型配置不存在: " + modelCode);
        }
        return getEmbeddingModelByModelId(config.getId());
    }

    /**
     * 刷新客户端缓存
     */
    @Override
    public void refreshCache() {
        log.info("开始刷新AI客户端缓存...");
        chatClientCache.clear();
        embeddingModelCache.clear();
        log.info("AI客户端缓存刷新完成");
    }

    /**
     * 创建ChatClient实例
     * 
     * @param modelId 模型ID
     * @return ChatClient
     */
    private ChatClient createChatClient(Long modelId) {
        AIModelConfig config = modelConfigService.getById(modelId);
        if (config == null) {
            throw new RuntimeException("模型配置不存在: " + modelId);
        }

        // 检查模型状态：0-停用，1-启用
        if (config.getStatus() != null && config.getStatus() == 0) {
            throw new RuntimeException("模型已停用: " + config.getModelName());
        }

        log.info("创建ChatClient - 模型: {}, 提供商: {}", config.getModelName(), config.getProvider());

        try {
            // 根据提供商创建不同的ChatClient
            switch (config.getProvider().toLowerCase()) {
                case "deepseek":
                case "openai":
                    return createOpenAiCompatibleChatClient(config);
                case "anthropic":
                    return createAnthropicChatClient(config);
                case "localhost":
                    return createOpenAiCompatibleChatClient(config);
                default:
                    // 默认使用OpenAI兼容接口
                    log.warn("未知的提供商: {}，使用OpenAI兼容模式", config.getProvider());
                    return createOpenAiCompatibleChatClient(config);
            }
        } catch (Exception e) {
            log.error("创建ChatClient失败: {}", config.getModelName(), e);
            throw new RuntimeException("创建ChatClient失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建OpenAI兼容的ChatClient
     * 适用于：DeepSeek、OpenAI、本地模型等
     */
    private ChatClient createOpenAiCompatibleChatClient(AIModelConfig config) {
        // 构建聊天选项
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
            .model(config.getModelName())
            .temperature(config.getTemperature() != null ? config.getTemperature().doubleValue() : 0.7)
            .maxCompletionTokens(config.getMaxTokens() != null ? config.getMaxTokens() : 4096);

        // 可选参数
        if (config.getTopP() != null) {
            optionsBuilder.topP(config.getTopP().doubleValue());
        }
        if (config.getFrequencyPenalty() != null) {
            optionsBuilder.frequencyPenalty(config.getFrequencyPenalty().doubleValue());
        }
        if (config.getPresencePenalty() != null) {
            optionsBuilder.presencePenalty(config.getPresencePenalty().doubleValue());
        }

        OpenAiChatOptions options = optionsBuilder.build();

        // 使用注入的Builder创建ChatClient
        return chatClientBuilder
            .defaultOptions(options)
            .build();
    }

    /**
     * 创建Anthropic ChatClient
     * 注意：Spring AI目前对Anthropic的支持可能需要额外配置
     */
    private ChatClient createAnthropicChatClient(AIModelConfig config) {
        // TODO: 实现Anthropic客户端创建
        // 目前可以先使用OpenAI兼容模式，或者等待Spring AI官方支持
        log.warn("Anthropic provider暂未完全支持，使用OpenAI兼容模式");
        return createOpenAiCompatibleChatClient(config);
    }

    /**
     * 创建EmbeddingModel实例
     * 
     * @param modelId 模型ID
     * @return EmbeddingModel
     */
    private EmbeddingModel createEmbeddingModel(Long modelId) {
        AIModelConfig config = modelConfigService.getById(modelId);
        if (config == null) {
            throw new RuntimeException("模型配置不存在: " + modelId);
        }

        // 检查模型状态：0-停用，1-启用
        if (config.getStatus() != null && config.getStatus() == 0) {
            throw new RuntimeException("模型已停用: " + config.getModelName());
        }

        log.info("创建EmbeddingModel - 模型: {}, 提供商: {}", config.getModelName(), config.getProvider());

        try {
            // 根据提供商创建不同的EmbeddingModel
            switch (config.getProvider().toLowerCase()) {
                case "deepseek":
                case "openai":
                    return createOpenAiCompatibleEmbeddingModel(config);
                case "localhost":
                    return createOpenAiCompatibleEmbeddingModel(config);
                default:
                    log.warn("未知的提供商: {}，使用默认EmbeddingModel", config.getProvider());
                    return createOpenAiCompatibleEmbeddingModel(config);
            }
        } catch (Exception e) {
            log.error("创建EmbeddingModel失败: {}", config.getModelName(), e);
            throw new RuntimeException("创建EmbeddingModel失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建OpenAI兼容的EmbeddingModel
     * 适用于：本地Embedding服务(如bge-large-zh-v1.5)、OpenAI Embedding等
     */
    private EmbeddingModel createOpenAiCompatibleEmbeddingModel(AIModelConfig config) {
        String baseUrl = config.getApiBaseUrl();
        String apiKey = config.getApiKey() != null ? config.getApiKey() : "sk-xxx";

        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .embeddingsPath("/v1/embeddings");

        OpenAiApi openAiApi = apiBuilder.build();

        return new OpenAiEmbeddingModel(openAiApi);
    }
}

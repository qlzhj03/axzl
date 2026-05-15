package com.axzl.biz.config;

import com.axzl.biz.service.PromptTemplateLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring AI ChatClient 配置
 * 基于 DeepSeek API（OpenAI 兼容接口）
 * 系统提示词从 prompts/axzl-question-system.st 模板文件加载
 *
 * @author liuchao
 */
@Configuration
public class AIChatConfig {

    /**
     * 创建通用 ChatClient Bean，用于AI对话
     * 系统提示词从 PromptTemplateLoader 加载
     */
    @Primary
    @Bean("generalChatClient")
    public ChatClient chatClient(ChatClient.Builder builder, PromptTemplateLoader promptTemplateLoader) {
        return builder
                .defaultSystem(promptTemplateLoader.getSystemPrompt())
                .build();
    }

    /**
     * 创建专用于问诊场景的 ChatClient Bean
     * 系统提示词从 PromptTemplateLoader 加载
     */
    @Bean("consultationChatClient")
    public ChatClient consultationChatClient(ChatClient.Builder builder, PromptTemplateLoader promptTemplateLoader) {
        return builder
                .defaultSystem(promptTemplateLoader.getSystemPrompt())
                .build();
    }
}

package com.axzl.biz.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring AI ChatClient 配置
 * 基于 DeepSeek API（OpenAI 兼容接口）
 *
 * @author liuchao
 */
@Configuration
public class AIChatConfig {

    /**
     * 创建通用 ChatClient Bean，用于AI对话
     */
    @Primary
    @Bean("generalChatClient")
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一位专业的医疗健康AI助手，名为"安心诊聊助手"。
                        你需要：
                        1. 基于用户的症状描述，结合医学知识给出分析和建议
                        2. 询问更多相关症状以便更准确地判断
                        3. 推荐可能的科室方向
                        4. 提醒用户：AI建议仅供参考，如症状严重请及时就医
                        5. 保持专业、温和、耐心的语气
                        6. 避免给出确定性的诊断结论，应使用"可能""建议""不排除"等措辞
                        """)
                .build();
    }

    /**
     * 创建专用于问诊场景的 ChatClient Bean
     */
    @Bean("consultationChatClient")
    public ChatClient consultationChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一位专业的医疗健康AI助手，名为"安心诊聊助手"。
                        你需要：
                        1. 基于用户的症状描述，结合医学知识给出分析和建议
                        2. 询问更多相关症状以便更准确地判断
                        3. 推荐可能的科室方向
                        4. 提醒用户：AI建议仅供参考，如症状严重请及时就医
                        5. 保持专业、温和、耐心的语气
                        6. 避免给出确定性的诊断结论，应使用"可能""建议""不排除"等措辞
                        """)
                .build();
    }
}

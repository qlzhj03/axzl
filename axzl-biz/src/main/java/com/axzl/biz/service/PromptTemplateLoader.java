package com.axzl.biz.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 医疗提示词模板加载器
 * 负责加载系统提示词模板、提供紧急关键词检测和免责声明
 *
 * @author liuchao
 */
@Component
public class PromptTemplateLoader {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateLoader.class);

    /** 系统提示词（从 prompts/axzl-question-system.st 加载） */
    private String systemPrompt;

    /** 回答格式要求 */
    private String responseFormatRules;

    /** 医疗免责声明文本 */
    public static final String MEDICAL_DISCLAIMER =
            "\n\n---\n> 【AI免责声明】本助手生成的内容仅供参考，不能替代专业医疗建议、诊断或治疗。如有身体不适，请务必及时前往正规医疗机构就诊。";

    /** 紧急症状关键词 */
    private static final Set<String> EMERGENCY_KEYWORDS = Set.of(
            "胸痛", "胸闷", "剧烈胸痛", "心绞痛",
            "呼吸困难", "窒息", "喘不过气", "气促",
            "大出血", "呕血", "咯血", "大量出血",
            "昏迷", "意识丧失", "不省人事", "晕倒",
            "抽搐", "惊厥", "癫痫", "高热惊厥",
            "严重外伤", "骨折", "大面积烧伤",
            "心脏骤停", "心跳停止", "猝死",
            "中风", "脑梗", "脑出血", "偏瘫",
            "剧烈腹痛", "急性腹痛",
            "自杀", "自残", "伤害自己", "伤害他人",
            "药物过量", "中毒", "误食农药"
    );

    /** 紧急情况预警前缀 */
    public static final String EMERGENCY_PREFIX =
            "【紧急预警】用户描述的症状中可能包含紧急情况。在回答中必须首先明确告知用户："
                    + "如症状紧急（如胸痛、呼吸困难、大出血、意识丧失等），请立即拨打120急救电话或前往最近医院急诊科。"
                    + "在给出紧急建议后，再提供常规的医学分析。\n\n";

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/axzl-question-system.st");
            this.systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
            log.info("系统提示词模板加载成功，长度: {} 字符", systemPrompt.length());
        } catch (Exception e) {
            log.error("加载系统提示词模板失败，使用内置默认提示词", e);
            this.systemPrompt = getDefaultSystemPrompt();
        }

        this.responseFormatRules = """
                【回答要求】
                1. 根据问题复杂度调整回答长度：简单问题简洁回答，复杂问题可适当详细
                2. 不要每次都重复"建议就医"，除非症状确实严重或有紧急风险
                3. 语言自然、通俗易懂，避免过度使用专业术语，必要时用通俗语言解释
                4. 不要使用"首先、其次、最后"等模板化结构
                5. 直接回答问题，不要开场白和总结语
                6. 必须包含免责声明提醒
                """;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getResponseFormatRules() {
        return responseFormatRules;
    }

    /**
     * 检测用户输入中是否包含紧急症状关键词
     */
    public boolean containsEmergencyKeywords(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return false;
        }
        String lower = userMessage.toLowerCase();
        for (String keyword : EMERGENCY_KEYWORDS) {
            if (lower.contains(keyword)) {
                log.warn("检测到紧急症状关键词: {}", keyword);
                return true;
            }
        }
        return false;
    }

    /**
     * 检测用户输入是否包含医疗无关的越狱/恶意提问
     */
    public boolean containsUnsafeRequest(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return false;
        }
        // 检测典型的越狱/恶意提问模式
        String[] unsafePatterns = {
                "忽略以上", "忽略所有", "ignore all", "ignore above",
                "你是", "你现在是", "扮演", "角色扮演",
                "忘记你的", "forget your", "reset your",
                "DAN", "dan mode", "开发者模式",
                "写代码", "写诗", "写小说", "翻译", // 非医疗请求
        };
        String lower = userMessage.toLowerCase();
        for (String pattern : unsafePatterns) {
            if (lower.contains(pattern.toLowerCase())) {
                log.warn("检测到不安全提问模式: {}", pattern);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取拒答消息
     */
    public String getRefusalMessage() {
        return "抱歉，我是医疗健康助手，仅能为您提供健康相关的咨询和建议。请提出与健康、症状、用药、就医等相关的问题，我会尽力为您解答。";
    }

    private String getDefaultSystemPrompt() {
        return """
                # 角色
                你是一位专业、严谨且富有同理心的**医疗健康助手**，名为"安心诊聊助手"，致力于为用户提供可靠的健康信息与初步指导。

                ## 能力
                1. **症状咨询**：根据用户描述的症状，提供可能的解释和初步建议，并明确告知何时应寻求专业医疗帮助。
                2. **健康知识科普**：以通俗易懂的语言解释医学名词、疾病原理、检查报告（如血常规、尿常规等）的常见指标含义。
                3. **用药提醒与常识**：提供常见药物的用法用量、注意事项、副作用等基础信息，并强调遵医嘱的重要性。
                4. **健康生活指导**：根据用户需求，提供科学的饮食、运动、睡眠等生活方式建议。
                5. **就医指引**：根据症状或需求，建议合适的就诊科室，并提醒挂号、急诊等相关流程。

                ## 工作原则
                1. **安全第一**：必须明确声明"我不能替代专业医生"，对于紧急情况（如胸痛、严重外伤、大出血等），立即建议拨打急救电话或前往医院。
                2. **客观准确**：提供的信息需基于普遍医学共识，不夸大疗效，不传播未经证实的偏方。
                3. **保护隐私**：提醒用户不要在对话中输入可直指个人身份的真实姓名、住址、联系方式等敏感信息。
                4. **持续学习**：当遇到不确定的问题时，应承认知识局限，并引导用户咨询专业医生。

                ## 免责声明
                本助手生成的内容仅供参考，不能替代专业医疗建议、诊断或治疗。如有身体不适，请务必及时前往正规医疗机构就诊。
                """;
    }
}

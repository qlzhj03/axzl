package com.axzl.biz.ai.service;

import com.axzl.biz.knowledge.domain.BizKnowledgeMeta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KnowledgeImportService 单元测试
 * 测试向量文本构建逻辑
 *
 * @author liuchao
 */
class KnowledgeImportServiceTest {

    /**
     * 通过反射调用私有方法 buildVectorText
     */
    private String invokeBuildVectorText(BizKnowledgeMeta meta) throws Exception {
        KnowledgeImportService service = new KnowledgeImportService();
        Method method = KnowledgeImportService.class.getDeclaredMethod(
                "buildVectorText", BizKnowledgeMeta.class);
        method.setAccessible(true);
        return (String) method.invoke(service, meta);
    }

    @Test
    @DisplayName("完整知识元数据 - 应包含所有字段")
    void testBuildVectorText_Full() throws Exception {
        BizKnowledgeMeta meta = new BizKnowledgeMeta();
        meta.setQuestion("什么是高血压？");
        meta.setAnswer("高血压是指血液在血管中流动时对血管壁造成的压力值持续高于正常的现象。");
        meta.setLabel("心血管内科");
        meta.setRelatedDiseases("心脏病,脑卒中");

        String result = invokeBuildVectorText(meta);

        assertTrue(result.contains("什么是高血压？"));
        assertTrue(result.contains("高血压是指血液"));
        assertTrue(result.contains("[心血管内科]"));
        assertTrue(result.contains("心脏病,脑卒中"));
    }

    @Test
    @DisplayName("仅有问题 - 应只包含问题文本")
    void testBuildVectorText_QuestionOnly() throws Exception {
        BizKnowledgeMeta meta = new BizKnowledgeMeta();
        meta.setQuestion("感冒是什么？");
        meta.setAnswer(null);
        meta.setLabel(null);
        meta.setRelatedDiseases(null);

        String result = invokeBuildVectorText(meta);

        assertEquals("感冒是什么？", result);
    }

    @Test
    @DisplayName("问题加答案 - 应空格分隔")
    void testBuildVectorText_QuestionAndAnswer() throws Exception {
        BizKnowledgeMeta meta = new BizKnowledgeMeta();
        meta.setQuestion("糖尿病怎么治疗？");
        meta.setAnswer("糖尿病需要综合治疗，包括饮食控制和药物治疗。");

        String result = invokeBuildVectorText(meta);

        assertTrue(result.startsWith("糖尿病怎么治疗？"));
        assertTrue(result.contains("饮食控制"));
        // 问题和答案之间应有空格
        assertTrue(result.contains(" "));
    }

    @Test
    @DisplayName("空元数据 - 应返回空字符串或仅含格式标记")
    void testBuildVectorText_Empty() throws Exception {
        BizKnowledgeMeta meta = new BizKnowledgeMeta();

        String result = invokeBuildVectorText(meta);

        // 空元数据应返回空字符串（或只含空格）
        assertNotNull(result);
        assertTrue(result.trim().isEmpty());
    }

    @Test
    @DisplayName("只有标签 - 应只显示标签")
    void testBuildVectorText_LabelOnly() throws Exception {
        BizKnowledgeMeta meta = new BizKnowledgeMeta();
        meta.setLabel("神经内科");

        String result = invokeBuildVectorText(meta);

        assertTrue(result.contains("[神经内科]"));
    }
}

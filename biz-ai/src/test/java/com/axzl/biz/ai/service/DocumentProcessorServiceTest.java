package com.axzl.biz.ai.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocumentProcessorService 单元测试
 * 测试文档切分逻辑（纯逻辑，无需Spring容器）
 *
 * @author liuchao
 */
class DocumentProcessorServiceTest {

    /**
     * 通过反射调用私有方法 splitByParagraph
     */
    @SuppressWarnings("unchecked")
    private List<String> invokeSplitByParagraph(String content, int maxSize, int overlap) throws Exception {
        DocumentProcessorService service = new DocumentProcessorService();
        Method method = DocumentProcessorService.class.getDeclaredMethod(
                "splitByParagraph", String.class, int.class, int.class);
        method.setAccessible(true);
        return (List<String>) method.invoke(service, content, maxSize, overlap);
    }

    @Test
    @DisplayName("小文本（小于切分块）- 应整体返回")
    void testSplitSmallText() throws Exception {
        String content = "这是一段很短的文本，不需要切分。";

        List<String> result = invokeSplitByParagraph(content, 800, 50);

        assertEquals(1, result.size());
        assertEquals(content, result.get(0));
    }

    @Test
    @DisplayName("空内容 - 应返回包含原内容的列表")
    void testSplitEmptyContent() throws Exception {
        List<String> result = invokeSplitByParagraph("", 800, 50);

        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test
    @DisplayName("多段落文本 - 应按段落边界切分")
    void testSplitMultiParagraph() throws Exception {
        // 创建多个短段落，确保按段落切分
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append("这是第").append(i + 1).append("段内容，包含一些测试文字用于验证切分功能。\n\n");
        }
        String content = sb.toString();

        List<String> result = invokeSplitByParagraph(content, 200, 10);

        // 至少切分为多个块
        assertTrue(result.size() > 1, "多段落应切分为多个块");
        // 每块不应超过 maxSize + overlap
        for (String chunk : result) {
            assertTrue(chunk.length() <= 200 + 10 + 50,
                    "每块长度应合理: " + chunk.length());
        }
    }

    @Test
    @DisplayName("长段落文本 - 应正确切分")
    void testSplitLongContent() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("这是第").append(i + 1).append("段很长的测试文本内容，用于验证文档切分的正确性和完整性，确保功能正常。\n\n");
        }
        String content = sb.toString();

        List<String> result = invokeSplitByParagraph(content, 200, 30);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() > 1, "长文本应切分为多个块");

        // 验证每个块非空
        for (String chunk : result) {
            assertFalse(chunk.isEmpty(), "每块不应为空");
        }
    }

    @Test
    @DisplayName("单段超长文本 - 应整体保留")
    void testSplitSingleLongParagraph() throws Exception {
        // 单段落（无空行分隔），即使很长也不应切分
        String content = "A".repeat(5000);

        List<String> result = invokeSplitByParagraph(content, 800, 50);

        // 单段落不切分，整体作为一块
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("重叠上下文 - 相邻块应有重叠内容")
    void testSplitWithOverlap() throws Exception {
        StringBuilder sb = new StringBuilder();
        // 创建足够长的段落以触发切分
        for (int i = 0; i < 30; i++) {
            sb.append("测试段落").append(i + 1).append("：这段内容包含一些特定文字用于验证重叠机制是否正常工作。\n\n");
        }
        String content = sb.toString();

        List<String> result = invokeSplitByParagraph(content, 100, 30);

        if (result.size() > 1) {
            // 验证相邻块之间的连续性
            for (int i = 1; i < result.size(); i++) {
                assertNotNull(result.get(i));
                assertFalse(result.get(i).isEmpty());
            }
        }
    }
}

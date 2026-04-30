package com.axzl.biz.knowledge.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 知识库实体单元测试
 *
 * @author liuchao
 */
class KnowledgeEntitiesTest {

    // ==================== BizKnowledgeMeta 测试 ====================

    @Test
    @DisplayName("BizKnowledgeMeta - @TableName注解存在")
    void testMetaTableNameAnnotation() {
        TableName annotation = BizKnowledgeMeta.class.getAnnotation(TableName.class);
        assertNotNull(annotation, "应有@TableName注解");
        assertEquals("biz_knowledge_meta", annotation.value());
    }

    @Test
    @DisplayName("BizKnowledgeMeta - @TableLogic注解存在")
    void testMetaTableLogic() throws Exception {
        Field delFlagField = BizKnowledgeMeta.class.getDeclaredField("delFlag");
        TableLogic annotation = delFlagField.getAnnotation(TableLogic.class);
        assertNotNull(annotation, "delFlag应有@TableLogic注解");
    }

    @Test
    @DisplayName("BizKnowledgeMeta - 所有字段getter/setter正常")
    void testMetaFieldAccess() {
        BizKnowledgeMeta meta = new BizKnowledgeMeta();
        Date now = new Date();

        meta.setId(1L);
        meta.setQuestion("问题");
        meta.setAnswer("答案");
        meta.setLabel("心血管内科");
        meta.setScore(95);
        meta.setRelatedDiseases("高血压");
        meta.setVectorId("vec_001");
        meta.setVectorStatus("1");
        meta.setSourceType("qa");
        meta.setSourceId(1L);
        meta.setChunkIndex(0);
        meta.setSource("管理员");
        meta.setStatus("0");
        meta.setCreateBy("admin");
        meta.setCreateTime(now);
        meta.setUpdateBy("admin");
        meta.setUpdateTime(now);
        meta.setRemark("备注");

        assertAll("字段getter验证",
                () -> assertEquals(1L, meta.getId()),
                () -> assertEquals("问题", meta.getQuestion()),
                () -> assertEquals("答案", meta.getAnswer()),
                () -> assertEquals("心血管内科", meta.getLabel()),
                () -> assertEquals(95, meta.getScore()),
                () -> assertEquals("高血压", meta.getRelatedDiseases()),
                () -> assertEquals("vec_001", meta.getVectorId()),
                () -> assertEquals("1", meta.getVectorStatus()),
                () -> assertEquals("qa", meta.getSourceType()),
                () -> assertEquals(1L, meta.getSourceId()),
                () -> assertEquals(0, meta.getChunkIndex()),
                () -> assertEquals("管理员", meta.getSource()),
                () -> assertEquals("0", meta.getStatus()),
                () -> assertEquals("admin", meta.getCreateBy()),
                () -> assertEquals(now, meta.getCreateTime()),
                () -> assertEquals("备注", meta.getRemark())
        );
    }

    @Test
    @DisplayName("BizKnowledgeMeta - toString包含关键字段")
    void testMetaToString() {
        BizKnowledgeMeta meta = new BizKnowledgeMeta();
        meta.setId(100L);
        meta.setQuestion("测试问题");

        String str = meta.toString();
        assertTrue(str.contains("100"));
        assertTrue(str.contains("测试问题"));
    }

    // ==================== BizDocument 测试 ====================

    @Test
    @DisplayName("BizDocument - @TableName注解存在")
    void testDocTableNameAnnotation() {
        TableName annotation = BizDocument.class.getAnnotation(TableName.class);
        assertNotNull(annotation);
        assertEquals("biz_document", annotation.value());
    }

    @Test
    @DisplayName("BizDocument - 序列化接口实现")
    void testDocSerializable() {
        BizDocument doc = new BizDocument();
        assertTrue(doc instanceof java.io.Serializable);
    }

    @Test
    @DisplayName("BizDocument - 所有字段getter/setter正常")
    void testDocFieldAccess() {
        BizDocument doc = new BizDocument();
        Date now = new Date();

        doc.setId(1L);
        doc.setFileName("测试文档.docx");
        doc.setFileType("word");
        doc.setFileSize(102400L);
        doc.setMilvusFilePath("/files/2026/04/test.docx");
        doc.setExtractedContent("文档提取内容");
        doc.setChunkCount(5);
        doc.setVectorStatus("2");
        doc.setUploadBy(100L);
        doc.setStatus("0");
        doc.setCreateTime(now);
        doc.setUpdateTime(now);
        doc.setRemark("测试备注");

        assertAll("BizDocument字段验证",
                () -> assertEquals(1L, doc.getId()),
                () -> assertEquals("测试文档.docx", doc.getFileName()),
                () -> assertEquals("word", doc.getFileType()),
                () -> assertEquals(102400L, doc.getFileSize()),
                () -> assertEquals("/files/2026/04/test.docx", doc.getMilvusFilePath()),
                () -> assertEquals("文档提取内容", doc.getExtractedContent()),
                () -> assertEquals(5, doc.getChunkCount()),
                () -> assertEquals("2", doc.getVectorStatus()),
                () -> assertEquals(100L, doc.getUploadBy()),
                () -> assertEquals("0", doc.getStatus()),
                () -> assertEquals(now, doc.getCreateTime()),
                () -> assertEquals("测试备注", doc.getRemark())
        );
    }
}

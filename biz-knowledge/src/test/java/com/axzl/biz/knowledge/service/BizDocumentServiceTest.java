package com.axzl.biz.knowledge.service;

import com.axzl.biz.knowledge.domain.BizDocument;
import com.axzl.biz.knowledge.mapper.BizDocumentMapper;
import com.axzl.biz.knowledge.service.impl.BizDocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BizDocumentService 单元测试
 * 测试文档服务的查询过滤、CRUD 逻辑
 *
 * @author liuchao
 */
class BizDocumentServiceTest {

    private BizDocumentMapper documentMapper;
    private BizDocumentServiceImpl documentService;

    @BeforeEach
    void setUp() throws Exception {
        documentMapper = mock(BizDocumentMapper.class);
        documentService = new BizDocumentServiceImpl();
        // 通过反射设置 baseMapper
        Class<?> superClass = documentService.getClass().getSuperclass();
        Field baseMapperField = null;
        while (superClass != null) {
            try {
                baseMapperField = superClass.getDeclaredField("baseMapper");
                break;
            } catch (NoSuchFieldException e) {
                superClass = superClass.getSuperclass();
            }
        }
        if (baseMapperField != null) {
            baseMapperField.setAccessible(true);
            baseMapperField.set(documentService, documentMapper);
        }
    }

    @Test
    @DisplayName("selectDocumentList - 按fileName模糊查询")
    void testSelectDocumentListByFileName() {
        BizDocument query = new BizDocument();
        query.setFileName("test.pdf");
        when(documentMapper.selectList(any())).thenReturn(List.of());

        documentService.selectDocumentList(query);

        verify(documentMapper).selectList(any());
    }

    @Test
    @DisplayName("selectDocumentList - 按fileType查询")
    void testSelectDocumentListByFileType() {
        BizDocument query = new BizDocument();
        query.setFileType("pdf");
        when(documentMapper.selectList(any())).thenReturn(List.of());

        documentService.selectDocumentList(query);

        verify(documentMapper).selectList(any());
    }

    @Test
    @DisplayName("selectDocumentList - 按vectorStatus查询")
    void testSelectDocumentListByVectorStatus() {
        BizDocument query = new BizDocument();
        query.setVectorStatus("0");
        when(documentMapper.selectList(any())).thenReturn(List.of());

        documentService.selectDocumentList(query);

        verify(documentMapper).selectList(any());
    }

    @Test
    @DisplayName("selectDocumentList - 按uploadBy查询")
    void testSelectDocumentListByUploadBy() {
        BizDocument query = new BizDocument();
        query.setUploadBy(1L);
        when(documentMapper.selectList(any())).thenReturn(List.of());

        documentService.selectDocumentList(query);

        verify(documentMapper).selectList(any());
    }

    @Test
    @DisplayName("selectDocumentList - 多条件组合查询")
    void testSelectDocumentListMultipleConditions() {
        BizDocument query = new BizDocument();
        query.setFileName("test");
        query.setFileType("pdf");
        query.setVectorStatus("1");
        when(documentMapper.selectList(any())).thenReturn(List.of());

        documentService.selectDocumentList(query);

        verify(documentMapper).selectList(any());
    }

    @Test
    @DisplayName("selectDocumentById - 根据ID查询")
    void testSelectDocumentById() {
        BizDocument doc = new BizDocument();
        doc.setId(1L);
        doc.setFileName("文档1");
        when(documentMapper.selectById(1L)).thenReturn(doc);

        BizDocument result = documentService.selectDocumentById(1L);

        assertNotNull(result);
        assertEquals("文档1", result.getFileName());
    }

    @Test
    @DisplayName("insertDocument - 成功插入返回1")
    void testInsertDocumentSuccess() {
        BizDocument doc = new BizDocument();
        doc.setFileName("新文档");
        when(documentMapper.insert(any(BizDocument.class))).thenReturn(1);

        int result = documentService.insertDocument(doc);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("insertDocument - 插入失败返回0")
    void testInsertDocumentFail() {
        BizDocument doc = new BizDocument();
        when(documentMapper.insert(any(BizDocument.class))).thenReturn(0);

        int result = documentService.insertDocument(doc);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("updateDocument - 成功更新返回1")
    void testUpdateDocumentSuccess() {
        BizDocument doc = new BizDocument();
        doc.setId(1L);
        doc.setFileName("更新文档");
        when(documentMapper.updateById(any(BizDocument.class))).thenReturn(1);

        int result = documentService.updateDocument(doc);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("updateDocument - 更新失败返回0")
    void testUpdateDocumentFail() {
        BizDocument doc = new BizDocument();
        when(documentMapper.updateById(any(BizDocument.class))).thenReturn(0);

        int result = documentService.updateDocument(doc);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("deleteDocumentByIds - 批量删除")
    void testDeleteDocumentByIds() {
        when(documentMapper.deleteById(1L)).thenReturn(1);
        when(documentMapper.deleteById(2L)).thenReturn(1);
        when(documentMapper.deleteById(3L)).thenReturn(0);

        int result = documentService.deleteDocumentByIds(new Long[]{1L, 2L, 3L});

        assertEquals(2, result);
        verify(documentMapper, times(3)).deleteById(anyLong());
    }

    @Test
    @DisplayName("updateVectorStatus - 更新向量状态")
    void testUpdateVectorStatus() {
        when(documentMapper.updateById(any(BizDocument.class))).thenReturn(1);

        int result = documentService.updateVectorStatus(1L, "2");

        assertEquals(1, result);
        verify(documentMapper).updateById(any(BizDocument.class));
    }
}

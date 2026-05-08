package com.axzl.biz.service;

/**
 * 文档处理服务接口
 * 基于 Spring AI 标准接口实现文档解析、切分、向量化全流程
 *
 * @author liuchao
 */
public interface IDocumentProcessorService {

    /**
     * 异步处理文档：读取 → 切分 → 向量化 → 存储
     *
     * @param documentId 文档ID
     * @param fileContent 文件内容（字节数组）
     */
    void processDocument(Long documentId, byte[] fileContent);
}

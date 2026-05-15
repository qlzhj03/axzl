package com.axzl.biz.service;

/**
 * 知识库向量导入服务接口
 * 将MySQL中的知识元数据向量化并存储到Milvus
 *
 * @author liuchao
 */
public interface IKnowledgeImportService {

    /**
     * 导入单条知识到向量库
     *
     * @param knowledgeId 知识ID
     */
    void importSingleKnowledge(Long knowledgeId);

    /**
     * 异步导入单条知识到向量库
     *
     * @param knowledgeId 知识ID
     */
    void asyncImportSingleKnowledge(Long knowledgeId);

    /**
     * 批量导入未向量化的知识
     *
     * @return 导入数量
     */
    int importAllUnprocessed();

    /**
     * 异步批量导入所有未向量化的知识
     */
    void asyncImportAllUnprocessed();

    /**
     * 查询待同步的知识数量（未导入 + 失败）
     *
     * @return 待同步记录数
     */
    int countPendingSync();

    /**
     * 删除知识的向量（同步删除Milvus中的数据）
     *
     * @param knowledgeId 知识ID
     */
    void deleteKnowledgeVector(Long knowledgeId);
}

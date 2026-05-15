package com.axzl.biz.service.impl;

import com.axzl.biz.domain.BizKnowledgeMeta;
import com.axzl.biz.service.IBizKnowledgeMetaService;
import com.axzl.biz.service.IKnowledgeImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 知识库向量导入服务实现类
 * 将MySQL中的知识元数据向量化并存储到Milvus
 *
 * @author liuchao
 */
@Service
public class KnowledgeImportServiceImpl implements IKnowledgeImportService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeImportServiceImpl.class);

    /** 每批向量化条数，控制并发防止embedding模型过载 */
    private static final int BATCH_SIZE = 5;

    /** 批次间隔毫秒，让embedding服务有喘息时间 */
    private static final long BATCH_INTERVAL_MS = 500;

    @Autowired
    private IBizKnowledgeMetaService knowledgeMetaService;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    /**
     * 异步导入单条知识到向量库
     */
    @Override
    @Async
    public void asyncImportSingleKnowledge(Long knowledgeId) {
        importSingleKnowledge(knowledgeId);
    }

    /**
     * 导入单条知识到向量库
     *
     * @param knowledgeId 知识ID
     */
    @Override
    public void importSingleKnowledge(Long knowledgeId) {
        BizKnowledgeMeta meta = knowledgeMetaService.selectKnowledgeMetaById(knowledgeId);
        if (meta == null) {
            log.warn("知识不存在: {}", knowledgeId);
            return;
        }
        processBatch(Collections.singletonList(meta));
    }

    /**
     * 异步批量导入所有未向量化的知识
     */
    @Override
    @Async
    public void asyncImportAllUnprocessed() {
        int count = importAllUnprocessed();
        log.info("异步批量向量化完成，共处理 {} 条", count);
    }

    /**
     * 查询待同步的知识数量（未导入 + 失败）
     */
    @Override
    public int countPendingSync() {
        return (int) knowledgeMetaService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizKnowledgeMeta>()
                        .in(BizKnowledgeMeta::getVectorStatus, "0", "2"));
    }

    /**
     * 批量导入未向量化的知识（含失败重试）
     * 逐批处理，每批最多 BATCH_SIZE 条，每批完成后立即更新状态
     *
     * @return 总处理数量
     */
    @Override
    public int importAllUnprocessed() {
        // 查询未导入 + 失败的记录
        List<BizKnowledgeMeta> pending = new ArrayList<>();
        BizKnowledgeMeta unprocessedQuery = new BizKnowledgeMeta();
        unprocessedQuery.setVectorStatus("0");
        pending.addAll(knowledgeMetaService.selectKnowledgeMetaList(unprocessedQuery));

        BizKnowledgeMeta failedQuery = new BizKnowledgeMeta();
        failedQuery.setVectorStatus("2");
        pending.addAll(knowledgeMetaService.selectKnowledgeMetaList(failedQuery));

        if (pending.isEmpty()) {
            log.info("没有需要导入的知识");
            return 0;
        }

        log.info("待向量化知识总数: {}", pending.size());

        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalFailed = 0;

        // 逐批处理，每批 BATCH_SIZE 条
        for (int i = 0; i < pending.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, pending.size());
            List<BizKnowledgeMeta> batch = pending.subList(i, end);

            int batchIndex = i / BATCH_SIZE + 1;
            int totalBatches = (int) Math.ceil((double) pending.size() / BATCH_SIZE);
            log.info("处理批次 {}/{}，本批 {} 条", batchIndex, totalBatches, batch.size());

            BatchResult result = processBatch(batch);
            totalProcessed += batch.size();
            totalSuccess += result.successCount;
            totalFailed += result.failCount;

            log.info("批次 {}/{} 完成: 成功 {}，失败 {}", batchIndex, totalBatches, result.successCount, result.failCount);

            // 批次间隔，让embedding服务恢复
            if (end < pending.size()) {
                sleepBetweenBatches();
            }
        }

        log.info("向量化全部完成: 总计 {} 条，成功 {}，失败 {}", totalProcessed, totalSuccess, totalFailed);
        return totalProcessed;
    }

    /**
     * 处理单个批次（最多 BATCH_SIZE 条）
     * 向量化成功后立即更新该批次的状态
     */
    private BatchResult processBatch(List<BizKnowledgeMeta> batch) {
        BatchResult result = new BatchResult();
        List<Pair<BizKnowledgeMeta, Document>> docPairs = new ArrayList<>();

        // 1. 构建向量文档
        for (BizKnowledgeMeta meta : batch) {
            try {
                String text = buildVectorText(meta);
                Document document = new Document(
                        text,
                        Map.of(
                                "question", meta.getQuestion() != null ? meta.getQuestion() : "",
                                "answer", meta.getAnswer() != null ? meta.getAnswer() : "",
                                "label", meta.getLabel() != null ? meta.getLabel() : "",
                                "score", meta.getScore() != null ? meta.getScore() : 0,
                                "related_diseases", meta.getRelatedDiseases() != null ? meta.getRelatedDiseases() : "",
                                "source_type", meta.getSourceType() != null ? meta.getSourceType() : "qa",
                                "source_id", meta.getSourceId() != null ? meta.getSourceId() : 0,
                                "chunk_index", meta.getChunkIndex() != null ? meta.getChunkIndex() : 0
                        )
                );
                docPairs.add(new Pair<>(meta, document));
            } catch (Exception e) {
                log.error("构建向量文档失败, knowledgeId: {}, 错误: {}", meta.getId(), e.getMessage());
                markFailed(meta);
                result.failCount++;
            }
        }

        if (docPairs.isEmpty()) {
            return result;
        }

        // 2. 批量提交到 Milvus（embedding + 存储）
        try {
            List<Document> documents = docPairs.stream().map(p -> p.right).toList();
            vectorStore.add(documents);

            // 3. 成功：逐条更新 MySQL 状态
            for (int i = 0; i < docPairs.size(); i++) {
                Pair<BizKnowledgeMeta, Document> pair = docPairs.get(i);
                BizKnowledgeMeta meta = pair.left;
                Document doc = pair.right;
                meta.setVectorId(doc.getId());
                meta.setVectorStatus("1");
                meta.setUpdateTime(new Date());
                knowledgeMetaService.updateKnowledgeMeta(meta);
                result.successCount++;
            }
        } catch (Exception e) {
            log.error("批次向量化失败, 批次大小: {}, 错误: {}", docPairs.size(), e.getMessage());
            // 整批标记失败
            for (Pair<BizKnowledgeMeta, Document> pair : docPairs) {
                markFailed(pair.left);
                result.failCount++;
            }
        }

        return result;
    }

    private void markFailed(BizKnowledgeMeta meta) {
        meta.setVectorStatus("2");
        meta.setUpdateTime(new Date());
        knowledgeMetaService.updateKnowledgeMeta(meta);
    }

    private void sleepBetweenBatches() {
        try {
            Thread.sleep(BATCH_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("批次间隔等待被中断");
        }
    }

    /** 批次处理结果 */
    private static class BatchResult {
        int successCount;
        int failCount;
    }

    /** 简单配对容器 */
    private static class Pair<L, R> {
        final L left;
        final R right;
        Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }
    }

    /**
     * 删除知识的向量（同步删除Milvus中的数据）
     *
     * @param knowledgeId 知识ID
     */
    @Override
    public void deleteKnowledgeVector(Long knowledgeId) {
        BizKnowledgeMeta meta = knowledgeMetaService.selectKnowledgeMetaById(knowledgeId);
        if (meta == null || meta.getVectorId() == null || meta.getVectorId().isEmpty()) {
            return;
        }
        try {
            vectorStore.delete(Collections.singletonList(meta.getVectorId()));
            log.info("已删除Milvus向量: {}", meta.getVectorId());
        } catch (Exception e) {
            log.error("删除Milvus向量失败: {}", meta.getVectorId(), e);
        }
    }

    /**
     * 构建向量化文本
     */
    private String buildVectorText(BizKnowledgeMeta meta) {
        StringBuilder sb = new StringBuilder();
        if (meta.getQuestion() != null && !meta.getQuestion().isEmpty()) {
            sb.append(meta.getQuestion());
        }
        if (meta.getAnswer() != null && !meta.getAnswer().isEmpty()) {
            sb.append(" ").append(meta.getAnswer());
        }
        if (meta.getLabel() != null && !meta.getLabel().isEmpty()) {
            sb.append(" [").append(meta.getLabel()).append("]");
        }
        if (meta.getRelatedDiseases() != null && !meta.getRelatedDiseases().isEmpty()) {
            sb.append(" [疾病: ").append(meta.getRelatedDiseases()).append("]");
        }
        return sb.toString();
    }
}

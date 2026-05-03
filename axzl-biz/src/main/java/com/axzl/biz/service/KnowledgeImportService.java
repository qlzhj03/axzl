package com.axzl.biz.ai.service;

import com.axzl.biz.domain.BizKnowledgeMeta;
import com.axzl.biz.service.IBizKnowledgeMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 知识库向量导入服务
 * 将MySQL中的知识元数据向量化并存储到Milvus
 *
 * @author liuchao
 */
@Service
public class KnowledgeImportService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeImportService.class);

    /** 批量导入条数 */
    private static final int BATCH_SIZE = 20;

    @Autowired
    private IBizKnowledgeMetaService knowledgeMetaService;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    /**
     * 导入单条知识到向量库
     *
     * @param knowledgeId 知识ID
     */
    public void importSingleKnowledge(Long knowledgeId) {
        BizKnowledgeMeta meta = knowledgeMetaService.selectKnowledgeMetaById(knowledgeId);
        if (meta == null) {
            log.warn("知识不存在: {}", knowledgeId);
            return;
        }
        importKnowledgeBatch(Collections.singletonList(meta));
    }

    /**
     * 批量导入未向量化的知识
     *
     * @return 导入数量
     */
    public int importAllUnprocessed() {
        BizKnowledgeMeta query = new BizKnowledgeMeta();
        query.setVectorStatus("0");
        List<BizKnowledgeMeta> unprocessed = knowledgeMetaService.selectKnowledgeMetaList(query);

        if (unprocessed.isEmpty()) {
            log.info("没有需要导入的知识");
            return 0;
        }

        int totalImported = 0;
        for (int i = 0; i < unprocessed.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, unprocessed.size());
            List<BizKnowledgeMeta> batch = unprocessed.subList(i, end);
            importKnowledgeBatch(batch);
            totalImported += batch.size();
            log.info("已导入 {}/{} 条知识", totalImported, unprocessed.size());
        }

        return totalImported;
    }

    /**
     * 批量导入知识
     */
    private void importKnowledgeBatch(List<BizKnowledgeMeta> knowledgeList) {
        List<Document> documents = new ArrayList<>();
        Map<Long, String> idToVectorId = new HashMap<>();

        for (BizKnowledgeMeta meta : knowledgeList) {
            try {
                // 构建向量化文本：问题 + 答案 + 标签
                String text = buildVectorText(meta);

                // 构建Milvus Document
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
                documents.add(document);
            } catch (Exception e) {
                log.error("构建知识向量文档失败: {}, 错误: {}", meta.getId(), e.getMessage());
            }
        }

        if (documents.isEmpty()) {
            return;
        }

        try {
            // 批量存储到Milvus
            vectorStore.add(documents);

            // 更新MySQL中的向量状态和向量ID
            for (int i = 0; i < documents.size() && i < knowledgeList.size(); i++) {
                BizKnowledgeMeta meta = knowledgeList.get(i);
                meta.setVectorId(documents.get(i).getId());
                meta.setVectorStatus("1");
                meta.setUpdateTime(new Date());
                knowledgeMetaService.updateKnowledgeMeta(meta);
            }
        } catch (Exception e) {
            log.error("批量导入到Milvus失败", e);
            // 标记为失败
            for (BizKnowledgeMeta meta : knowledgeList) {
                meta.setVectorStatus("2");
                meta.setUpdateTime(new Date());
                knowledgeMetaService.updateKnowledgeMeta(meta);
            }
        }
    }

    /**
     * 删除知识的向量（同步删除Milvus中的数据）
     *
     * @param knowledgeId 知识ID
     */
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

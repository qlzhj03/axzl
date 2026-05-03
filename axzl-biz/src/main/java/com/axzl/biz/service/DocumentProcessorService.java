package com.axzl.biz.service;

import com.axzl.biz.domain.BizDocument;
import com.axzl.biz.domain.BizKnowledgeMeta;
import com.axzl.biz.utils.CustomDocumentReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 文档处理服务
 * 基于 Spring AI 标准接口实现文档解析、切分、向量化全流程
 *
 * @author liuchao
 */
@Service
public class DocumentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorService.class);

    /** 小文档阈值：小于此字符数则不切分 */
    private static final int SMALL_DOC_THRESHOLD = 3000;

    /** 切分块大小 */
    private static final int CHUNK_SIZE = 800;

    /** 重叠字符数 */
    private static final int CHUNK_OVERLAP = 50;

    /** 批量向量化条数 */
    private static final int BATCH_SIZE = 20;

    @Autowired
    private IBizDocumentService documentService;

    @Autowired
    private IBizKnowledgeMetaService knowledgeMetaService;

    @Autowired
    @Lazy
    private SpringAIClientFactory clientFactory;

    @Autowired
    private VectorStore vectorStore;

    private EmbeddingModel getEmbeddingModel() {
        return clientFactory.getDefaultEmbeddingModel();
    }

    private final CustomDocumentReader documentReader = new CustomDocumentReader();

    /**
     * 异步处理文档：读取 → 切分 → 向量化 → 存储
     *
     * @param documentId 文档ID
     * @param fileContent 文件内容（字节数组）
     */
    @Async
    public void processDocument(Long documentId, byte[] fileContent) {
        log.info("开始处理文档 ID: {}", documentId);

        BizDocument bizDocument = documentService.selectDocumentById(documentId);
        if (bizDocument == null) {
            log.error("文档不存在: {}", documentId);
            return;
        }

        try {
            // 1. 更新状态为处理中
            documentService.updateVectorStatus(documentId, "1");

            // 2. 读取文档内容
            String content = documentReader.readDocument(
                    new java.io.ByteArrayInputStream(fileContent),
                    bizDocument.getFileType());

            // 保存提取的文本内容
            bizDocument.setExtractedContent(content);
            documentService.updateDocument(bizDocument);

            // 3. 切分文档
            List<String> chunks;
            if (content.length() < SMALL_DOC_THRESHOLD) {
                // 小文档：整体向量化
                chunks = Collections.singletonList(content);
                log.info("文档 {} 较小({}字符)，整体向量化", documentId, content.length());
            } else {
                // 大文档：按段落切分
                chunks = splitByParagraph(content, CHUNK_SIZE, CHUNK_OVERLAP);
                log.info("文档 {} 较大({}字符)，切分为 {} 块", documentId, content.length(), chunks.size());
            }

            // 4. 更新切分块数
            bizDocument.setChunkCount(chunks.size());
            documentService.updateDocument(bizDocument);

            // 5. 批量向量化并存储
            List<BizKnowledgeMeta> knowledgeMetas = new ArrayList<>();
            int chunkIndex = 0;

            for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, chunks.size());
                List<String> batchChunks = chunks.subList(i, end);

                // 批量向量化
                List<float[]> embeddings = new ArrayList<>();
                for (String chunk : batchChunks) {
                    float[] embedding = getEmbeddingModel().embed(chunk);
                    embeddings.add(embedding);
                }

                // 存储向量和知识元数据
                for (int j = 0; j < batchChunks.size(); j++) {
                    String chunk = batchChunks.get(j);
                    float[] embedding = embeddings.get(j);

                    // 创建Milvus Document
                    Document milvusDoc = new Document(
                            chunk,
                            Map.of(
                                    "question", chunk,
                                    "answer", "",
                                    "label", "",
                                    "score", 0,
                                    "related_diseases", "",
                                    "source_type", "doc",
                                    "source_id", documentId,
                                    "chunk_index", chunkIndex
                            )
                    );

                    // 存储到Milvus
                    vectorStore.add(List.of(milvusDoc));

                    // 创建MySQL知识元数据记录
                    BizKnowledgeMeta meta = new BizKnowledgeMeta();
                    meta.setQuestion(chunk);
                    meta.setAnswer("");
                    meta.setLabel("");
                    meta.setScore(0);
                    meta.setRelatedDiseases("");
                    meta.setVectorId(milvusDoc.getId());
                    meta.setVectorStatus("1");
                    meta.setSourceType("doc");
                    meta.setSourceId(documentId);
                    meta.setChunkIndex(chunkIndex);
                    meta.setSource(bizDocument.getFileName());
                    meta.setStatus("0");
                    meta.setCreateTime(new Date());
                    knowledgeMetas.add(meta);

                    chunkIndex++;
                }

                log.info("文档 {} 已处理 {}/{} 块", documentId, chunkIndex, chunks.size());
            }

            // 6. 批量保存知识元数据
            for (BizKnowledgeMeta meta : knowledgeMetas) {
                knowledgeMetaService.insertKnowledgeMeta(meta);
            }

            // 7. 更新状态为已完成
            documentService.updateVectorStatus(documentId, "2");
            log.info("文档 {} 处理完成，共 {} 块", documentId, chunks.size());

        } catch (Exception e) {
            log.error("文档 {} 处理失败", documentId, e);
            documentService.updateVectorStatus(documentId, "3");
        }
    }

    /**
     * 按段落切分文本
     * 优先按段落边界切分，保证块的完整性
     */
    private List<String> splitByParagraph(String content, int maxSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        // 按段落分割
        String[] paragraphs = content.split("\\n\\s*\\n");

        StringBuilder currentChunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            if (currentChunk.length() + paragraph.length() > maxSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                // 保留重叠上下文
                if (currentChunk.length() > overlap) {
                    currentChunk = new StringBuilder(
                            currentChunk.substring(currentChunk.length() - overlap));
                } else {
                    currentChunk = new StringBuilder();
                }
            }

            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(paragraph);
        }

        // 最后一块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks.isEmpty() ? List.of(content) : chunks;
    }
}

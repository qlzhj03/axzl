package com.axzl.biz.service.impl;

import com.axzl.biz.domain.BizDocument;
import com.axzl.biz.domain.BizKnowledgeMeta;
import com.axzl.biz.service.IBizDocumentService;
import com.axzl.biz.service.IBizKnowledgeMetaService;
import com.axzl.biz.service.IDocumentProcessorService;
import com.axzl.biz.utils.CustomDocumentReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 文档处理服务实现类
 * 基于 Spring AI 标准接口实现文档解析、切分、向量化全流程
 *
 * @author liuchao
 */
@Service
public class DocumentProcessorServiceImpl implements IDocumentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorServiceImpl.class);

    /** 小文档阈值：小于此字符数则不切分 */
    private static final int SMALL_DOC_THRESHOLD = 3000;

    /** 切分块大小 */
    private static final int CHUNK_SIZE = 800;

    /** 重叠字符数 */
    private static final int CHUNK_OVERLAP = 50;

    @Autowired
    private IBizDocumentService documentService;

    @Autowired
    private IBizKnowledgeMetaService knowledgeMetaService;

    @Autowired
    private VectorStore vectorStore;

    private final CustomDocumentReader documentReader = new CustomDocumentReader();

    /**
     * 异步处理文档：读取 → 切分 → 向量化 → 存储
     * vectorStore.add() 内部会自动调用 EmbeddingModel 生成向量，无需手动调用 embed()
     *
     * @param documentId 文档ID
     * @param fileContent 文件内容（字节数组）
     */
    @Async
    @Override
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

            if (content == null || content.trim().isEmpty()) {
                log.error("文档 {} 提取内容为空", documentId);
                documentService.updateVectorStatus(documentId, "3");
                return;
            }

            // 保存提取的文本内容
            bizDocument.setExtractedContent(content);
            documentService.updateDocument(bizDocument);

            // 3. 切分文档
            List<String> chunks;
            if (content.length() < SMALL_DOC_THRESHOLD) {
                chunks = Collections.singletonList(content.trim());
                log.info("文档 {} 较小({}字符)，整体向量化", documentId, content.length());
            } else {
                chunks = splitByParagraph(content, CHUNK_SIZE, CHUNK_OVERLAP);
                log.info("文档 {} 较大({}字符)，切分为 {} 块", documentId, content.length(), chunks.size());
            }

            // 过滤空块
            chunks = chunks.stream().map(String::trim).filter(c -> !c.isEmpty()).toList();
            if (chunks.isEmpty()) {
                log.error("文档 {} 切分后无有效内容", documentId);
                documentService.updateVectorStatus(documentId, "3");
                return;
            }

            // 4. 更新切分块数
            bizDocument.setChunkCount(chunks.size());
            documentService.updateDocument(bizDocument);

            // 5. 逐块存储到 Milvus（VectorStore 内部自动 Embedding）
            List<BizKnowledgeMeta> knowledgeMetas = new ArrayList<>();
            int successCount = 0;
            int failedCount = 0;

            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);

                try {
                    // 创建 Document，VectorStore.add() 内部会自动调用 EmbeddingModel 生成向量
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
                                    "chunk_index", i
                            )
                    );

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
                    meta.setChunkIndex(i);
                    meta.setSource(bizDocument.getFileName());
                    meta.setStatus("0");
                    meta.setCreateTime(new Date());
                    knowledgeMetas.add(meta);

                    successCount++;
                } catch (Exception e) {
                    log.warn("文档 {} 第 {} 块存储失败，跳过: {}", documentId, i, e.getMessage());
                    failedCount++;
                }

                if ((i + 1) % 50 == 0) {
                    log.info("文档 {} 进度: {}/{}", documentId, i + 1, chunks.size());
                }
            }

            // 6. 批量保存知识元数据
            for (BizKnowledgeMeta meta : knowledgeMetas) {
                knowledgeMetaService.insertKnowledgeMeta(meta);
            }

            // 7. 更新状态
            if (successCount == 0) {
                documentService.updateVectorStatus(documentId, "3");
                log.error("文档 {} 所有块存储失败", documentId);
            } else {
                documentService.updateVectorStatus(documentId, "2");
                log.info("文档 {} 处理完成，共 {} 块 (成功 {}，失败 {})",
                        documentId, chunks.size(), successCount, failedCount);
            }

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

package com.axzl.web.controller.biz;

import com.axzl.biz.domain.BizDocument;
import com.axzl.biz.domain.BizKnowledgeMeta;
import com.axzl.biz.service.DocumentProcessorService;
import com.axzl.biz.service.IBizDocumentService;
import com.axzl.biz.service.IBizKnowledgeMetaService;
import com.axzl.common.annotation.Log;
import com.axzl.common.config.AxzlConfig;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import com.axzl.common.utils.file.FileUploadUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.Date;

/**
 * 知识库Controller（统一管理端和用户端）
 *
 * @author liuchao
 */
@Tag(name = "知识库管理", description = "知识库和文档管理相关接口")
@RestController
@RequestMapping("/biz/knowledge")
public class KnowledgeController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeController.class);

    @Autowired
    private IBizKnowledgeMetaService knowledgeMetaService;

    @Autowired
    private IBizDocumentService documentService;

    @Autowired
    private DocumentProcessorService documentProcessorService;

    // ==================== 管理端接口 - 知识管理 ====================

    /**
     * 查询知识库列表（管理端）
     */
    @Operation(summary = "查询知识库列表", description = "管理端：分页查询知识库元数据列表")
    @PreAuthorize("@ss.hasPermi('biz:knowledge:list')")
    @GetMapping("/admin/list")
    public TableDataInfo adminList(BizKnowledgeMeta knowledgeMeta) {
        startPage();
        return getDataTable(knowledgeMetaService.selectKnowledgeMetaList(knowledgeMeta));
    }

    /**
     * 查询知识详情（管理端）
     */
    @Operation(summary = "查询知识详情", description = "管理端：根据ID查询知识库详细信息")
    @PreAuthorize("@ss.hasPermi('biz:knowledge:query')")
    @GetMapping("/admin/{id}")
    public AjaxResult adminGetInfo(
            @Parameter(description = "知识库ID", required = true) @PathVariable Long id) {
        return success(knowledgeMetaService.selectKnowledgeMetaById(id));
    }

    /**
     * 新增知识（管理端）
     */
    @Operation(summary = "新增知识", description = "管理端：创建新的知识库条目")
    @PreAuthorize("@ss.hasPermi('biz:knowledge:add')")
    @Log(title = "知识库管理", businessType = BusinessType.INSERT)
    @PostMapping("/admin")
    public AjaxResult adminAdd(@Validated @RequestBody BizKnowledgeMeta knowledgeMeta) {
        return toAjax(knowledgeMetaService.insertKnowledgeMeta(knowledgeMeta));
    }

    /**
     * 更新知识（管理端）
     */
    @Operation(summary = "更新知识", description = "管理端：更新知识库条目信息")
    @PreAuthorize("@ss.hasPermi('biz:knowledge:edit')")
    @Log(title = "知识库管理", businessType = BusinessType.UPDATE)
    @PutMapping("/admin")
    public AjaxResult adminEdit(@Validated @RequestBody BizKnowledgeMeta knowledgeMeta) {
        return toAjax(knowledgeMetaService.updateKnowledgeMeta(knowledgeMeta));
    }

    /**
     * 删除知识（管理端）
     */
    @Operation(summary = "删除知识", description = "管理端：批量删除知识库条目")
    @PreAuthorize("@ss.hasPermi('biz:knowledge:remove')")
    @Log(title = "知识库管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/admin/{ids}")
    public AjaxResult adminRemove(
            @Parameter(description = "知识库ID数组", required = true) @PathVariable Long[] ids) {
        return toAjax(knowledgeMetaService.deleteKnowledgeMetaByIds(ids));
    }

    // ==================== 管理端接口 - 文档管理 ====================

    /**
     * 查询文档列表（管理端）
     */
    @Operation(summary = "查询文档列表", description = "管理端：分页查询文档列表")
    @PreAuthorize("@ss.hasPermi('biz:document:list')")
    @GetMapping("/admin/document/list")
    public TableDataInfo adminDocumentList(BizDocument document) {
        startPage();
        return getDataTable(documentService.selectDocumentList(document));
    }

    /**
     * 查询文档详情（管理端）
     */
    @Operation(summary = "查询文档详情", description = "管理端：根据ID查询文档详细信息")
    @PreAuthorize("@ss.hasPermi('biz:document:query')")
    @GetMapping("/admin/document/{id}")
    public AjaxResult adminDocumentGetInfo(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        return success(documentService.selectDocumentById(id));
    }

    /**
     * 删除文档（管理端）
     */
    @Operation(summary = "删除文档", description = "管理端：删除指定文档")
    @PreAuthorize("@ss.hasPermi('biz:document:remove')")
    @Log(title = "文档管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/admin/document/{id}")
    public AjaxResult adminDocumentRemove(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        Long[] ids = {id};
        return toAjax(documentService.deleteDocumentByIds(ids));
    }

    // ==================== 用户端接口 - 知识搜索 ====================

    /**
     * 语义搜索知识（用户端，匿名访问）
     */
    @Operation(summary = "语义搜索知识", description = "用户端：基于关键词和标签搜索知识库")
    @PostMapping("/search")
    public AjaxResult search(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "搜索请求参数", required = true) 
                             @RequestBody SearchRequest request) {
        // 使用模糊搜索作为语义搜索的基础实现
        // 实际语义搜索由biz-ai模块的AiConsultationService完成
        LambdaQueryWrapper<BizKnowledgeMeta> wrapper = new LambdaQueryWrapper<>();
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(BizKnowledgeMeta::getQuestion, request.getKeyword())
                   .or()
                   .like(BizKnowledgeMeta::getAnswer, request.getKeyword());
        }
        if (request.getLabel() != null && !request.getLabel().isEmpty()) {
            wrapper.eq(BizKnowledgeMeta::getLabel, request.getLabel());
        }
        wrapper.eq(BizKnowledgeMeta::getVectorStatus, "1")
               .orderByDesc(BizKnowledgeMeta::getScore)
               .last("limit 20");
        return success(knowledgeMetaService.list(wrapper));
    }

    /**
     * 知识详情（用户端，匿名访问）
     */
    @Operation(summary = "知识详情", description = "用户端：获取知识库条目详细信息")
    @GetMapping("/{id}")
    public AjaxResult getInfo(
            @Parameter(description = "知识库ID", required = true) @PathVariable Long id) {
        return success(knowledgeMetaService.selectKnowledgeMetaById(id));
    }

    // ==================== 用户端接口 - 文档管理 ====================

    /**
     * 上传文档（用户端）
     * 完整流程：文件上传 → 保存到本地 → 创建数据库记录 → 异步触发向量化处理
     */
    @Operation(summary = "上传文档", description = "用户端：上传文档文件并自动进行向量化处理（支持txt/word/pdf/md/jsonl格式）")
    @PostMapping("/document/upload")
    public AjaxResult upload(
            @Parameter(description = "文档文件", required = true) @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return error("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return error("文件名不能为空");
        }

        // 检查文件类型（支持 md 和 jsonl）
        String fileType = getFileType(originalFilename);
        if (fileType == null) {
            return error("不支持的文件格式，仅支持 txt/word/pdf/md/jsonl");
        }

        try {
            // 1. 使用若依的文件上传工具保存文件到本地
            String filePath = AxzlConfig.getUploadPath();
            String fileName = FileUploadUtils.upload(filePath, file);
            log.info("文件上传成功: {}, 路径: {}", originalFilename, fileName);

            // 2. 读取文件内容为字节数组（用于后续向量化处理）
            byte[] fileContent = file.getBytes();

            // 3. 创建文档记录
            BizDocument document = new BizDocument();
            document.setFileName(originalFilename);
            document.setFileType(fileType);
            document.setFileSize(file.getSize());
            document.setMilvusFilePath(fileName); // 保存文件路径
            document.setVectorStatus("0"); // 未处理状态
            document.setUploadBy(getUserId());
            document.setCreateTime(new Date());
            document.setChunkCount(0);

            documentService.insertDocument(document);
            log.info("文档记录创建成功, ID: {}", document.getId());

            // 4. 异步触发文档向量化处理
            documentProcessorService.processDocument(document.getId(), fileContent);
            log.info("已启动文档向量化处理, ID: {}", document.getId());

            AjaxResult ajax = success(document);
            ajax.put("message", "文档上传成功，正在后台进行向量化处理");
            return ajax;

        } catch (Exception e) {
            log.error("文档上传失败: {}", originalFilename, e);
            return error("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 文档列表（用户端）
     */
    @Operation(summary = "文档列表", description = "用户端：查询当前用户上传的文档列表")
    @GetMapping("/document/list")
    public TableDataInfo documentList(BizDocument document) {
        startPage();
        // 用户只能看到自己上传的文档
        document.setUploadBy(getUserId());
        return getDataTable(documentService.selectDocumentList(document));
    }

    /**
     * 文档详情（用户端）
     */
    @Operation(summary = "文档详情", description = "用户端：获取文档详细信息")
    @GetMapping("/document/{id}")
    public AjaxResult documentGetInfo(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        return success(documentService.selectDocumentById(id));
    }

    /**
     * 删除文档（用户端）
     */
    @Operation(summary = "删除文档", description = "用户端：删除指定文档")
    @DeleteMapping("/document/{id}")
    public AjaxResult documentRemove(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        Long[] ids = {id};
        return toAjax(documentService.deleteDocumentByIds(ids));
    }

    /**
     * 重新处理文档（用户端）
     */
    @Operation(summary = "重新处理文档", description = "用户端：重新触发文档向量化处理")
    @PostMapping("/document/reprocess/{id}")
    public AjaxResult documentReprocess(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        try {
            BizDocument document = documentService.selectDocumentById(id);
            if (document == null) {
                return error("文档不存在");
            }

            // 检查文件是否存在
            String filePath = AxzlConfig.getProfile() + document.getMilvusFilePath();
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                return error("原文件不存在，无法重新处理");
            }

            // 读取文件内容
            byte[] fileContent = Files.readAllBytes(file.toPath());

            // 重置状态为未处理
            document.setVectorStatus("0");
            documentService.updateDocument(document);

            // 异步触发重新处理
            documentProcessorService.processDocument(id, fileContent);

            return success("已启动重新处理");
        } catch (Exception e) {
            log.error("重新处理文档失败: {}", id, e);
            return error("重新处理失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件名获取文件类型
     */
    private String getFileType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".txt")) {
            return "txt";
        } else if (lowerName.endsWith(".docx") || lowerName.endsWith(".doc")) {
            return "word";
        } else if (lowerName.endsWith(".pdf")) {
            return "pdf";
        } else if (lowerName.endsWith(".md")) {
            return "md";
        } else if (lowerName.endsWith(".jsonl")) {
            return "jsonl";
        }
        return null;
    }

    // ---- DTOs ----

    /**
     * 搜索请求DTO
     */
    public static class SearchRequest {
        private String keyword;
        private String label;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}

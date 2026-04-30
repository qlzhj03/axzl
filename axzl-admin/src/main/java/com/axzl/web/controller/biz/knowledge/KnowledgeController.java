package com.axzl.web.controller.biz.knowledge;

import com.axzl.biz.knowledge.domain.BizDocument;
import com.axzl.biz.knowledge.domain.BizKnowledgeMeta;
import com.axzl.biz.knowledge.service.IBizDocumentService;
import com.axzl.biz.knowledge.service.IBizKnowledgeMetaService;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 知识库Controller（统一管理端和用户端）
 *
 * @author liuchao
 */
@RestController
@RequestMapping("/biz/knowledge")
public class KnowledgeController extends BaseController {

    @Autowired
    private IBizKnowledgeMetaService knowledgeMetaService;

    @Autowired
    private IBizDocumentService documentService;

    // ==================== 管理端接口 - 知识管理 ====================

    /**
     * 查询知识库列表（管理端）
     */
    @PreAuthorize("@ss.hasPermi('biz:knowledge:list')")
    @GetMapping("/admin/list")
    public TableDataInfo adminList(BizKnowledgeMeta knowledgeMeta) {
        startPage();
        return getDataTable(knowledgeMetaService.selectKnowledgeMetaList(knowledgeMeta));
    }

    /**
     * 查询知识详情（管理端）
     */
    @PreAuthorize("@ss.hasPermi('biz:knowledge:query')")
    @GetMapping("/admin/{id}")
    public AjaxResult adminGetInfo(@PathVariable Long id) {
        return success(knowledgeMetaService.selectKnowledgeMetaById(id));
    }

    /**
     * 新增知识（管理端）
     */
    @PreAuthorize("@ss.hasPermi('biz:knowledge:add')")
    @Log(title = "知识库管理", businessType = BusinessType.INSERT)
    @PostMapping("/admin")
    public AjaxResult adminAdd(@Validated @RequestBody BizKnowledgeMeta knowledgeMeta) {
        return toAjax(knowledgeMetaService.insertKnowledgeMeta(knowledgeMeta));
    }

    /**
     * 更新知识（管理端）
     */
    @PreAuthorize("@ss.hasPermi('biz:knowledge:edit')")
    @Log(title = "知识库管理", businessType = BusinessType.UPDATE)
    @PutMapping("/admin")
    public AjaxResult adminEdit(@Validated @RequestBody BizKnowledgeMeta knowledgeMeta) {
        return toAjax(knowledgeMetaService.updateKnowledgeMeta(knowledgeMeta));
    }

    /**
     * 删除知识（管理端）
     */
    @PreAuthorize("@ss.hasPermi('biz:knowledge:remove')")
    @Log(title = "知识库管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/admin/{ids}")
    public AjaxResult adminRemove(@PathVariable Long[] ids) {
        return toAjax(knowledgeMetaService.deleteKnowledgeMetaByIds(ids));
    }

    // ==================== 管理端接口 - 文档管理 ====================

    /**
     * 查询文档列表（管理端）
     */
    @PreAuthorize("@ss.hasPermi('biz:document:list')")
    @GetMapping("/admin/document/list")
    public TableDataInfo adminDocumentList(BizDocument document) {
        startPage();
        return getDataTable(documentService.selectDocumentList(document));
    }

    /**
     * 查询文档详情（管理端）
     */
    @PreAuthorize("@ss.hasPermi('biz:document:query')")
    @GetMapping("/admin/document/{id}")
    public AjaxResult adminDocumentGetInfo(@PathVariable Long id) {
        return success(documentService.selectDocumentById(id));
    }

    /**
     * 删除文档（管理端）
     */
    @PreAuthorize("@ss.hasPermi('biz:document:remove')")
    @Log(title = "文档管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/admin/document/{id}")
    public AjaxResult adminDocumentRemove(@PathVariable Long id) {
        Long[] ids = {id};
        return toAjax(documentService.deleteDocumentByIds(ids));
    }

    // ==================== 用户端接口 - 知识搜索 ====================

    /**
     * 语义搜索知识（用户端，匿名访问）
     */
    @PostMapping("/search")
    public AjaxResult search(@RequestBody SearchRequest request) {
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
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(knowledgeMetaService.selectKnowledgeMetaById(id));
    }

    // ==================== 用户端接口 - 文档管理 ====================

    /**
     * 上传文档（用户端）
     */
    @PostMapping("/document/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return error("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return error("文件名不能为空");
        }

        // 检查文件类型
        String fileType = getFileType(originalFilename);
        if (fileType == null) {
            return error("不支持的文件格式，仅支持 txt/word/pdf/md");
        }

        // 创建文档记录
        BizDocument document = new BizDocument();
        document.setFileName(originalFilename);
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setVectorStatus("0");
        document.setUploadBy(getUserId());
        document.setCreateTime(new Date());
        document.setChunkCount(0);

        documentService.insertDocument(document);
        return success(document);
    }

    /**
     * 文档列表（用户端）
     */
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
    @GetMapping("/document/{id}")
    public AjaxResult documentGetInfo(@PathVariable Long id) {
        return success(documentService.selectDocumentById(id));
    }

    /**
     * 删除文档（用户端）
     */
    @DeleteMapping("/document/{id}")
    public AjaxResult documentRemove(@PathVariable Long id) {
        Long[] ids = {id};
        return toAjax(documentService.deleteDocumentByIds(ids));
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

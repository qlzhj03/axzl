package com.axzl.web.controller.biz;

import com.axzl.biz.domain.BizLearningArticle;
import com.axzl.biz.service.IBizLearningArticleService;
import com.axzl.biz.service.IBizLearningUserArticleService;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康知识文章Controller
 *
 * @author liuchao
 */
@Tag(name = "健康知识文章管理", description = "健康知识文章相关接口")
@RestController
@RequestMapping("/biz/learning/article")
public class LearningArticleController extends BaseController {

    @Autowired
    private IBizLearningArticleService articleService;

    @Autowired
    private IBizLearningUserArticleService userArticleService;

    // ==================== 管理端接口 ====================

    /**
     * 查询文章列表（管理端）
     */
    @Operation(summary = "查询文章列表", description = "管理端：分页查询文章列表")
    @PreAuthorize("@ss.hasPermi('biz:learning:article:list')")
    @GetMapping("/admin/list")
    public TableDataInfo adminList(BizLearningArticle article) {
        startPage();
        List<BizLearningArticle> list = articleService.selectArticleList(article);
        return getDataTable(list);
    }

    /**
     * 获取文章详细信息（管理端）
     */
    @Operation(summary = "获取文章详情", description = "管理端：根据ID查询文章详细信息")
    @PreAuthorize("@ss.hasPermi('biz:learning:article:query')")
    @GetMapping("/admin/{articleId}")
    public AjaxResult adminGetInfo(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        return success(articleService.selectArticleById(articleId));
    }

    /**
     * 新增文章（管理端）
     */
    @Operation(summary = "新增文章", description = "管理端：创建新的健康知识文章")
    @PreAuthorize("@ss.hasPermi('biz:learning:article:add')")
    @Log(title = "健康知识文章", businessType = BusinessType.INSERT)
    @PostMapping("/admin")
    public AjaxResult adminAdd(@Validated @RequestBody BizLearningArticle article) {
        return toAjax(articleService.insertArticle(article));
    }

    /**
     * 修改文章（管理端）
     */
    @Operation(summary = "修改文章", description = "管理端：更新健康知识文章信息")
    @PreAuthorize("@ss.hasPermi('biz:learning:article:edit')")
    @Log(title = "健康知识文章", businessType = BusinessType.UPDATE)
    @PutMapping("/admin")
    public AjaxResult adminEdit(@Validated @RequestBody BizLearningArticle article) {
        return toAjax(articleService.updateArticle(article));
    }

    /**
     * 删除文章（管理端）
     */
    @Operation(summary = "删除文章", description = "管理端：批量删除健康知识文章")
    @PreAuthorize("@ss.hasPermi('biz:learning:article:remove')")
    @Log(title = "健康知识文章", businessType = BusinessType.DELETE)
    @DeleteMapping("/admin/{articleIds}")
    public AjaxResult adminRemove(
            @Parameter(description = "文章ID数组", required = true) @PathVariable Long[] articleIds) {
        return toAjax(articleService.deleteArticleByIds(articleIds));
    }

    /**
     * 发布/下架文章（管理端）
     */
    @Operation(summary = "发布/下架文章", description = "管理端：切换文章发布状态")
    @PreAuthorize("@ss.hasPermi('biz:learning:article:edit')")
    @Log(title = "健康知识文章", businessType = BusinessType.UPDATE)
    @PutMapping("/admin/publish/{articleId}")
    public AjaxResult publish(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        BizLearningArticle article = articleService.selectArticleById(articleId);
        if (article == null) {
            return error("文章不存在");
        }
        if ("1".equals(article.getStatus())) {
            article.setStatus("2");
        } else {
            article.setStatus("1");
            article.setPublishTime(new Date());
        }
        return toAjax(articleService.updateArticle(article));
    }

    /**
     * AI生成摘要（管理端）
     */
    @Operation(summary = "AI生成摘要", description = "管理端：使用AI为文章生成摘要")
    @PreAuthorize("@ss.hasPermi('biz:learning:article:edit')")
    @Log(title = "健康知识文章-AI摘要", businessType = BusinessType.UPDATE)
    @PostMapping("/admin/ai-summary/{articleId}")
    public AjaxResult generateAiSummary(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        String summary = articleService.generateAiSummary(articleId);
        if (summary != null) {
            AjaxResult result = success();
            result.put("data", summary);
            return result;
        }
        return error("AI生成摘要失败");
    }

    // ==================== 患者端接口 ====================

    /**
     * 查询已发布文章列表（患者端）
     */
    @Operation(summary = "查询已发布文章", description = "患者端：查询已发布的文章列表")
    @GetMapping("/list")
    public TableDataInfo list(BizLearningArticle article) {
        startPage();
        List<BizLearningArticle> list = articleService.selectPublishedArticles(article);
        return getDataTable(list);
    }

    /**
     * 阅读文章详情（患者端）
     */
    @Operation(summary = "阅读文章", description = "患者端：获取文章详情并增加阅读次数")
    @GetMapping("/{articleId}")
    public AjaxResult readArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        // 增加阅读次数
        articleService.incrementViewCount(articleId);
        // 获取详情（含用户互动状态）
        BizLearningArticle article = articleService.selectArticleDetail(articleId, getUserId());
        return success(article);
    }

    /**
     * 切换收藏（患者端）
     */
    @Operation(summary = "切换收藏", description = "患者端：收藏/取消收藏文章")
    @PostMapping("/bookmark/{articleId}")
    public AjaxResult toggleBookmark(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        userArticleService.toggleBookmark(getUserId(), articleId);
        return success();
    }

    /**
     * 切换点赞（患者端）
     */
    @Operation(summary = "切换点赞", description = "患者端：点赞/取消点赞文章")
    @PostMapping("/like/{articleId}")
    public AjaxResult toggleLike(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        userArticleService.toggleLike(getUserId(), articleId);
        return success();
    }

    /**
     * 更新阅读进度（患者端）
     */
    @Operation(summary = "更新阅读进度", description = "患者端：更新文章阅读进度和时长")
    @PutMapping("/progress")
    public AjaxResult updateProgress(@RequestBody Map<String, Object> params) {
        Long articleId = Long.valueOf(params.get("articleId").toString());
        Integer progress = params.get("progress") != null ? Integer.valueOf(params.get("progress").toString()) : null;
        Integer duration = params.get("duration") != null ? Integer.valueOf(params.get("duration").toString()) : null;
        userArticleService.updateReadProgress(getUserId(), articleId, progress, duration);
        return success();
    }

    /**
     * 获取收藏文章列表（患者端）
     */
    @Operation(summary = "获取收藏列表", description = "患者端：获取当前用户收藏的文章")
    @GetMapping("/bookmarks")
    public AjaxResult getBookmarks() {
        return success(userArticleService.getBookmarkedArticles(getUserId()));
    }

    /**
     * 获取推荐文章（患者端）
     */
    @Operation(summary = "获取推荐文章", description = "患者端：获取个性化推荐文章")
    @GetMapping("/recommended")
    public AjaxResult getRecommended() {
        return success(articleService.getRecommendedArticles(getUserId(), 10));
    }

    /**
     * 获取学习统计（患者端）
     */
    @Operation(summary = "获取学习统计", description = "患者端：获取当前用户的学习统计数据")
    @GetMapping("/stats")
    public AjaxResult getStats() {
        return success(userArticleService.getUserLearningStats(getUserId()));
    }
}

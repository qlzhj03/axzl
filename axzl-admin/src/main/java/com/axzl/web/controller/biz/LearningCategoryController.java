package com.axzl.web.controller.biz;

import com.axzl.biz.domain.BizLearningCategory;
import com.axzl.biz.service.IBizLearningCategoryService;
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

import java.util.List;

/**
 * 健康知识分类Controller
 *
 * @author liuchao
 */
@Tag(name = "健康知识分类管理", description = "健康知识分类相关接口")
@RestController
@RequestMapping("/biz/learning/category")
public class LearningCategoryController extends BaseController {

    @Autowired
    private IBizLearningCategoryService categoryService;

    /**
     * 查询分类列表
     */
    @Operation(summary = "查询分类列表", description = "分页查询健康知识分类列表")
    @PreAuthorize("@ss.hasPermi('biz:learning:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLearningCategory category) {
        startPage();
        List<BizLearningCategory> list = categoryService.selectCategoryList(category);
        return getDataTable(list);
    }

    /**
     * 获取分类详细信息
     */
    @Operation(summary = "获取分类详情", description = "根据ID查询分类详细信息")
    @PreAuthorize("@ss.hasPermi('biz:learning:category:query')")
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
        return success(categoryService.selectCategoryById(categoryId));
    }

    /**
     * 新增分类
     */
    @Operation(summary = "新增分类", description = "创建新的健康知识分类")
    @PreAuthorize("@ss.hasPermi('biz:learning:category:add')")
    @Log(title = "健康知识分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BizLearningCategory category) {
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改分类
     */
    @Operation(summary = "修改分类", description = "更新健康知识分类信息")
    @PreAuthorize("@ss.hasPermi('biz:learning:category:edit')")
    @Log(title = "健康知识分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BizLearningCategory category) {
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除分类
     */
    @Operation(summary = "删除分类", description = "批量删除健康知识分类")
    @PreAuthorize("@ss.hasPermi('biz:learning:category:remove')")
    @Log(title = "健康知识分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(
            @Parameter(description = "分类ID数组", required = true) @PathVariable Long[] categoryIds) {
        return toAjax(categoryService.deleteCategoryByIds(categoryIds));
    }

    /**
     * 获取分类树（下拉选择用，登录即可访问）
     */
    @Operation(summary = "获取分类树", description = "获取分类树形结构，用于下拉选择")
    @GetMapping("/tree")
    public AjaxResult tree() {
        return success(categoryService.selectCategoryTree());
    }
}

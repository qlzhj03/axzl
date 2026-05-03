package com.axzl.biz.service;

import com.axzl.biz.domain.BizLearningCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 健康知识分类 服务层
 *
 * @author liuchao
 */
public interface IBizLearningCategoryService extends IService<BizLearningCategory> {

    /**
     * 查询分类列表
     */
    List<BizLearningCategory> selectCategoryList(BizLearningCategory category);

    /**
     * 通过ID查询分类
     */
    BizLearningCategory selectCategoryById(Long categoryId);

    /**
     * 新增分类
     */
    int insertCategory(BizLearningCategory category);

    /**
     * 修改分类
     */
    int updateCategory(BizLearningCategory category);

    /**
     * 批量删除分类
     */
    int deleteCategoryByIds(Long[] ids);

    /**
     * 查询分类树
     */
    List<BizLearningCategory> selectCategoryTree();
}

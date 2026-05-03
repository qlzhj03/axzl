package com.axzl.biz.service.impl;

import com.axzl.biz.domain.BizLearningCategory;
import com.axzl.biz.mapper.BizLearningCategoryMapper;
import com.axzl.biz.service.IBizLearningCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康知识分类 服务层实现
 *
 * @author liuchao
 */
@Service
public class BizLearningCategoryServiceImpl extends ServiceImpl<BizLearningCategoryMapper, BizLearningCategory>
        implements IBizLearningCategoryService {

    @Override
    public List<BizLearningCategory> selectCategoryList(BizLearningCategory category) {
        LambdaQueryWrapper<BizLearningCategory> wrapper = new LambdaQueryWrapper<>();
        if (category.getCategoryName() != null && !category.getCategoryName().isEmpty()) {
            wrapper.like(BizLearningCategory::getCategoryName, category.getCategoryName());
        }
        if (category.getStatus() != null && !category.getStatus().isEmpty()) {
            wrapper.eq(BizLearningCategory::getStatus, category.getStatus());
        }
        if (category.getParentId() != null) {
            wrapper.eq(BizLearningCategory::getParentId, category.getParentId());
        }
        wrapper.orderByAsc(BizLearningCategory::getSortOrder);
        return list(wrapper);
    }

    @Override
    public BizLearningCategory selectCategoryById(Long categoryId) {
        return getById(categoryId);
    }

    @Override
    public int insertCategory(BizLearningCategory category) {
        return save(category) ? 1 : 0;
    }

    @Override
    public int updateCategory(BizLearningCategory category) {
        return updateById(category) ? 1 : 0;
    }

    @Override
    public int deleteCategoryByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            if (removeById(id)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<BizLearningCategory> selectCategoryTree() {
        LambdaQueryWrapper<BizLearningCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizLearningCategory::getStatus, "0");
        wrapper.orderByAsc(BizLearningCategory::getSortOrder);
        List<BizLearningCategory> allCategories = list(wrapper);

        // 按parentId分组
        Map<Long, List<BizLearningCategory>> parentMap = allCategories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));

        // 递归构建树
        return buildTree(parentMap, 0L);
    }

    private List<BizLearningCategory> buildTree(Map<Long, List<BizLearningCategory>> parentMap, Long parentId) {
        List<BizLearningCategory> tree = new ArrayList<>();
        List<BizLearningCategory> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        for (BizLearningCategory child : children) {
            child.setChildren(buildTree(parentMap, child.getCategoryId()));
            tree.add(child);
        }
        return tree;
    }
}

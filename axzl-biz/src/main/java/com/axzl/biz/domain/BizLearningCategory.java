package com.axzl.biz.domain;

import com.axzl.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Map;

/**
 * 健康知识分类表 biz_learning_category
 *
 * @author liuchao
 */
@TableName("biz_learning_category")
public class BizLearningCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 请求参数 */
    @TableField(exist = false)
    @JsonIgnore
    private Map<String, Object> params;

    /** 子分类列表（非数据库字段） */
    @TableField(exist = false)
    private List<BizLearningCategory> children;

    /** 分类ID */
    @TableId(type = IdType.AUTO)
    private Long categoryId;

    /** 父分类ID(0=顶级) */
    private Long parentId;

    /** 分类名称 */
    private String categoryName;

    /** 显示顺序 */
    private Integer sortOrder;

    /** 分类图标 */
    private String icon;

    /** 状态(0正常 1停用) */
    private String status;

    /** 删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public List<BizLearningCategory> getChildren() {
        return children;
    }

    public void setChildren(List<BizLearningCategory> children) {
        this.children = children;
    }

    public Map<String, Object> getParams() {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("categoryId", getCategoryId())
                .append("parentId", getParentId())
                .append("categoryName", getCategoryName())
                .append("sortOrder", getSortOrder())
                .append("icon", getIcon())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("remark", getRemark())
                .toString();
    }
}

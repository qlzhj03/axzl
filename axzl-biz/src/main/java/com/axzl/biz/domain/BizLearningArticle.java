package com.axzl.biz.domain;

import com.axzl.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.Map;

/**
 * 健康知识文章表 biz_learning_article
 *
 * @author liuchao
 */
@TableName("biz_learning_article")
public class BizLearningArticle extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 请求参数 */
    @TableField(exist = false)
    @JsonIgnore
    private Map<String, Object> params;

    /** 分类名称（非数据库字段，用于展示） */
    @TableField(exist = false)
    private String categoryName;

    /** 是否已收藏（非数据库字段，用于患者端） */
    @TableField(exist = false)
    private Integer isBookmarked;

    /** 阅读进度（非数据库字段，用于患者端） */
    @TableField(exist = false)
    private Integer readProgress;

    /** 文章ID */
    @TableId(type = IdType.AUTO)
    private Long articleId;

    /** 分类ID */
    private Long categoryId;

    /** 文章标题 */
    private String title;

    /** 文章摘要 */
    private String summary;

    /** 文章内容(富文本) */
    private String content;

    /** 封面图片URL */
    private String coverImage;

    /** 作者 */
    private String author;

    /** 来源 */
    private String source;

    /** 标签(逗号分隔) */
    private String tags;

    /** 阅读次数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** AI生成摘要 */
    private String aiSummary;

    /** 是否置顶(0否 1是) */
    private Integer isTop;

    /** 是否推荐(0否 1是) */
    private Integer isFeatured;

    /** 状态(0草稿 1已发布 2已下架) */
    private String status;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public Integer getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Integer isFeatured) {
        this.isFeatured = isFeatured;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getIsBookmarked() {
        return isBookmarked;
    }

    public void setIsBookmarked(Integer isBookmarked) {
        this.isBookmarked = isBookmarked;
    }

    public Integer getReadProgress() {
        return readProgress;
    }

    public void setReadProgress(Integer readProgress) {
        this.readProgress = readProgress;
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
                .append("articleId", getArticleId())
                .append("categoryId", getCategoryId())
                .append("title", getTitle())
                .append("author", getAuthor())
                .append("viewCount", getViewCount())
                .append("likeCount", getLikeCount())
                .append("status", getStatus())
                .append("publishTime", getPublishTime())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .toString();
    }
}

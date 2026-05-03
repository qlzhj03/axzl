package com.axzl.biz.service;

import com.axzl.biz.domain.BizLearningArticle;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 健康知识文章 服务层
 *
 * @author liuchao
 */
public interface IBizLearningArticleService extends IService<BizLearningArticle> {

    /**
     * 查询文章列表（管理端）
     */
    List<BizLearningArticle> selectArticleList(BizLearningArticle article);

    /**
     * 查询已发布文章列表（患者端）
     */
    List<BizLearningArticle> selectPublishedArticles(BizLearningArticle article);

    /**
     * 通过ID查询文章
     */
    BizLearningArticle selectArticleById(Long articleId);

    /**
     * 查询文章详情（包含用户互动状态）
     */
    BizLearningArticle selectArticleDetail(Long articleId, Long userId);

    /**
     * 新增文章
     */
    int insertArticle(BizLearningArticle article);

    /**
     * 修改文章
     */
    int updateArticle(BizLearningArticle article);

    /**
     * 批量删除文章
     */
    int deleteArticleByIds(Long[] ids);

    /**
     * 增加阅读次数
     */
    void incrementViewCount(Long articleId);

    /**
     * AI生成摘要
     */
    String generateAiSummary(Long articleId);

    /**
     * 获取推荐文章
     */
    List<BizLearningArticle> getRecommendedArticles(Long userId, int limit);
}

package com.axzl.biz.service;

import com.axzl.biz.domain.BizLearningArticle;
import com.axzl.biz.domain.BizLearningUserArticle;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 用户文章互动记录 服务层
 *
 * @author liuchao
 */
public interface IBizLearningUserArticleService extends IService<BizLearningUserArticle> {

    /**
     * 切换收藏状态
     */
    void toggleBookmark(Long userId, Long articleId);

    /**
     * 切换点赞状态
     */
    void toggleLike(Long userId, Long articleId);

    /**
     * 更新阅读进度
     */
    void updateReadProgress(Long userId, Long articleId, Integer progress, Integer duration);

    /**
     * 获取用户文章互动记录
     */
    BizLearningUserArticle getUserArticleRecord(Long userId, Long articleId);

    /**
     * 获取用户收藏的文章列表
     */
    List<BizLearningArticle> getBookmarkedArticles(Long userId);

    /**
     * 获取用户学习统计
     */
    Map<String, Object> getUserLearningStats(Long userId);
}

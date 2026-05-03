package com.axzl.biz.service.impl;

import com.axzl.biz.domain.BizLearningArticle;
import com.axzl.biz.domain.BizLearningUserArticle;
import com.axzl.biz.mapper.BizLearningArticleMapper;
import com.axzl.biz.service.IBizLearningArticleService;
import com.axzl.biz.service.IBizLearningUserArticleService;
import com.axzl.biz.service.SpringAIClientFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 健康知识文章 服务层实现
 *
 * @author liuchao
 */
@Service
public class BizLearningArticleServiceImpl extends ServiceImpl<BizLearningArticleMapper, BizLearningArticle>
        implements IBizLearningArticleService {

    private static final Logger log = LoggerFactory.getLogger(BizLearningArticleServiceImpl.class);

    @Autowired
    private SpringAIClientFactory clientFactory;

    @Autowired
    private IBizLearningUserArticleService userArticleService;

    @Override
    public List<BizLearningArticle> selectArticleList(BizLearningArticle article) {
        LambdaQueryWrapper<BizLearningArticle> wrapper = new LambdaQueryWrapper<>();
        if (article.getTitle() != null && !article.getTitle().isEmpty()) {
            wrapper.like(BizLearningArticle::getTitle, article.getTitle());
        }
        if (article.getCategoryId() != null) {
            wrapper.eq(BizLearningArticle::getCategoryId, article.getCategoryId());
        }
        if (article.getStatus() != null && !article.getStatus().isEmpty()) {
            wrapper.eq(BizLearningArticle::getStatus, article.getStatus());
        }
        if (article.getAuthor() != null && !article.getAuthor().isEmpty()) {
            wrapper.like(BizLearningArticle::getAuthor, article.getAuthor());
        }
        wrapper.orderByDesc(BizLearningArticle::getIsTop)
               .orderByDesc(BizLearningArticle::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<BizLearningArticle> selectPublishedArticles(BizLearningArticle article) {
        LambdaQueryWrapper<BizLearningArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizLearningArticle::getStatus, "1");
        if (article.getTitle() != null && !article.getTitle().isEmpty()) {
            wrapper.like(BizLearningArticle::getTitle, article.getTitle());
        }
        if (article.getCategoryId() != null) {
            wrapper.eq(BizLearningArticle::getCategoryId, article.getCategoryId());
        }
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            wrapper.like(BizLearningArticle::getTags, article.getTags());
        }
        wrapper.orderByDesc(BizLearningArticle::getIsTop)
               .orderByDesc(BizLearningArticle::getIsFeatured)
               .orderByDesc(BizLearningArticle::getPublishTime);
        return list(wrapper);
    }

    @Override
    public BizLearningArticle selectArticleById(Long articleId) {
        return getById(articleId);
    }

    @Override
    public BizLearningArticle selectArticleDetail(Long articleId, Long userId) {
        BizLearningArticle article = getById(articleId);
        if (article != null && userId != null) {
            BizLearningUserArticle userRecord = userArticleService.getUserArticleRecord(userId, articleId);
            if (userRecord != null) {
                article.setIsBookmarked(userRecord.getIsBookmarked());
                article.setReadProgress(userRecord.getReadProgress());
            }
        }
        return article;
    }

    @Override
    public int insertArticle(BizLearningArticle article) {
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        if (article.getLikeCount() == null) {
            article.setLikeCount(0);
        }
        if (article.getIsTop() == null) {
            article.setIsTop(0);
        }
        if (article.getIsFeatured() == null) {
            article.setIsFeatured(0);
        }
        return save(article) ? 1 : 0;
    }

    @Override
    public int updateArticle(BizLearningArticle article) {
        return updateById(article) ? 1 : 0;
    }

    @Override
    public int deleteArticleByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            if (removeById(id)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void incrementViewCount(Long articleId) {
        LambdaUpdateWrapper<BizLearningArticle> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BizLearningArticle::getArticleId, articleId)
               .setSql("view_count = view_count + 1");
        update(wrapper);
    }

    @Override
    public String generateAiSummary(Long articleId) {
        BizLearningArticle article = getById(articleId);
        if (article == null || article.getContent() == null || article.getContent().isEmpty()) {
            return null;
        }

        try {
            // 截取内容前3000字符用于生成摘要
            String content = article.getContent();
            if (content.length() > 3000) {
                content = content.substring(0, 3000);
            }

            String prompt = "请为以下健康科普文章生成一段200字以内的中文摘要，要求简洁准确，突出核心健康知识点：\n\n"
                    + "标题：" + article.getTitle() + "\n\n"
                    + "内容：" + content;

            String summary = clientFactory.getDefaultChatClient()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            // 更新文章的AI摘要
            article.setAiSummary(summary);
            updateById(article);

            return summary;
        } catch (Exception e) {
            log.error("AI生成摘要失败, articleId: {}", articleId, e);
            return null;
        }
    }

    @Override
    public List<BizLearningArticle> getRecommendedArticles(Long userId, int limit) {
        // 简单推荐策略：推荐置顶+精选文章，按发布时间倒序
        LambdaQueryWrapper<BizLearningArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizLearningArticle::getStatus, "1");
        wrapper.and(w -> w.eq(BizLearningArticle::getIsTop, 1).or().eq(BizLearningArticle::getIsFeatured, 1));
        wrapper.orderByDesc(BizLearningArticle::getIsTop)
               .orderByDesc(BizLearningArticle::getIsFeatured)
               .orderByDesc(BizLearningArticle::getPublishTime)
               .last("LIMIT " + limit);
        return list(wrapper);
    }
}

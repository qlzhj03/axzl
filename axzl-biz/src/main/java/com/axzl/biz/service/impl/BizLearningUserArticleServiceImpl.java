package com.axzl.biz.service.impl;

import com.axzl.biz.domain.BizLearningArticle;
import com.axzl.biz.domain.BizLearningUserArticle;
import com.axzl.biz.mapper.BizLearningArticleMapper;
import com.axzl.biz.mapper.BizLearningUserArticleMapper;
import com.axzl.biz.service.IBizLearningUserArticleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户文章互动记录 服务层实现
 *
 * @author liuchao
 */
@Service
public class BizLearningUserArticleServiceImpl extends ServiceImpl<BizLearningUserArticleMapper, BizLearningUserArticle>
        implements IBizLearningUserArticleService {

    @Autowired
    private BizLearningArticleMapper articleMapper;

    @Override
    public void toggleBookmark(Long userId, Long articleId) {
        BizLearningUserArticle record = getOrCreateRecord(userId, articleId);
        record.setIsBookmarked(record.getIsBookmarked() != null && record.getIsBookmarked() == 1 ? 0 : 1);
        record.setUpdateTime(new Date());
        saveOrUpdate(record);
    }

    @Override
    public void toggleLike(Long userId, Long articleId) {
        BizLearningUserArticle record = getOrCreateRecord(userId, articleId);
        boolean wasLiked = record.getIsLiked() != null && record.getIsLiked() == 1;
        record.setIsLiked(wasLiked ? 0 : 1);
        record.setUpdateTime(new Date());
        saveOrUpdate(record);

        // 更新文章点赞数
        LambdaUpdateWrapper<BizLearningArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BizLearningArticle::getArticleId, articleId);
        if (wasLiked) {
            updateWrapper.setSql("like_count = GREATEST(like_count - 1, 0)");
        } else {
            updateWrapper.setSql("like_count = like_count + 1");
        }
        articleMapper.update(null, updateWrapper);
    }

    @Override
    public void updateReadProgress(Long userId, Long articleId, Integer progress, Integer duration) {
        BizLearningUserArticle record = getOrCreateRecord(userId, articleId);
        if (progress != null) {
            record.setReadProgress(Math.max(record.getReadProgress() != null ? record.getReadProgress() : 0, progress));
        }
        if (duration != null) {
            record.setReadDuration((record.getReadDuration() != null ? record.getReadDuration() : 0) + duration);
        }
        record.setLastReadTime(new Date());
        record.setUpdateTime(new Date());
        saveOrUpdate(record);
    }

    @Override
    public BizLearningUserArticle getUserArticleRecord(Long userId, Long articleId) {
        LambdaQueryWrapper<BizLearningUserArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizLearningUserArticle::getUserId, userId)
               .eq(BizLearningUserArticle::getArticleId, articleId);
        return getOne(wrapper);
    }

    @Override
    public List<BizLearningArticle> getBookmarkedArticles(Long userId) {
        LambdaQueryWrapper<BizLearningUserArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizLearningUserArticle::getUserId, userId)
               .eq(BizLearningUserArticle::getIsBookmarked, 1)
               .orderByDesc(BizLearningUserArticle::getUpdateTime);
        List<BizLearningUserArticle> records = list(wrapper);

        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> articleIds = records.stream()
                .map(BizLearningUserArticle::getArticleId)
                .collect(Collectors.toList());

        List<BizLearningArticle> articles = articleMapper.selectBatchIds(articleIds);

        // 按收藏记录顺序排列，并填充阅读进度
        Map<Long, BizLearningUserArticle> recordMap = records.stream()
                .collect(Collectors.toMap(BizLearningUserArticle::getArticleId, r -> r));
        Map<Long, BizLearningArticle> articleMap = articles.stream()
                .collect(Collectors.toMap(BizLearningArticle::getArticleId, a -> a));

        List<BizLearningArticle> result = new ArrayList<>();
        for (Long articleId : articleIds) {
            BizLearningArticle article = articleMap.get(articleId);
            if (article != null) {
                BizLearningUserArticle record = recordMap.get(articleId);
                article.setIsBookmarked(1);
                article.setReadProgress(record.getReadProgress());
                result.add(article);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getUserLearningStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 已读文章数（阅读进度 > 0）
        LambdaQueryWrapper<BizLearningUserArticle> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(BizLearningUserArticle::getUserId, userId)
                   .gt(BizLearningUserArticle::getReadProgress, 0);
        long readCount = count(readWrapper);

        // 收藏文章数
        LambdaQueryWrapper<BizLearningUserArticle> bookmarkWrapper = new LambdaQueryWrapper<>();
        bookmarkWrapper.eq(BizLearningUserArticle::getUserId, userId)
                       .eq(BizLearningUserArticle::getIsBookmarked, 1);
        long bookmarkCount = count(bookmarkWrapper);

        // 点赞文章数
        LambdaQueryWrapper<BizLearningUserArticle> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(BizLearningUserArticle::getUserId, userId)
                   .eq(BizLearningUserArticle::getIsLiked, 1);
        long likeCount = count(likeWrapper);

        // 总阅读时长（秒）
        LambdaQueryWrapper<BizLearningUserArticle> durationWrapper = new LambdaQueryWrapper<>();
        durationWrapper.eq(BizLearningUserArticle::getUserId, userId);
        List<BizLearningUserArticle> allRecords = list(durationWrapper);
        int totalDuration = allRecords.stream()
                .mapToInt(r -> r.getReadDuration() != null ? r.getReadDuration() : 0)
                .sum();

        stats.put("readCount", readCount);
        stats.put("bookmarkCount", bookmarkCount);
        stats.put("likeCount", likeCount);
        stats.put("totalDuration", totalDuration);
        return stats;
    }

    private BizLearningUserArticle getOrCreateRecord(Long userId, Long articleId) {
        BizLearningUserArticle record = getUserArticleRecord(userId, articleId);
        if (record == null) {
            record = new BizLearningUserArticle();
            record.setUserId(userId);
            record.setArticleId(articleId);
            record.setIsBookmarked(0);
            record.setIsLiked(0);
            record.setReadProgress(0);
            record.setReadDuration(0);
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            save(record);
        }
        return record;
    }
}

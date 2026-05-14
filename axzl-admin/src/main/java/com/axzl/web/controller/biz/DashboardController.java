package com.axzl.web.controller.biz;

import com.axzl.biz.mapper.*;
import com.axzl.common.constant.CacheConstants;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.redis.RedisCache;
import com.axzl.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 首页统计Controller
 *
 * @author liuchao
 */
@Tag(name = "首页统计", description = "首页数据概览统计接口")
@RestController
@RequestMapping("/biz/dashboard")
public class DashboardController extends BaseController {

    private final SysUserMapper userMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final UserProfileMapper profileMapper;
    private final BizDocumentMapper documentMapper;
    private final BizKnowledgeMetaMapper knowledgeMapper;
    private final RedisCache redisCache;

    public DashboardController(SysUserMapper userMapper,
                               ChatSessionMapper sessionMapper,
                               ChatMessageMapper messageMapper,
                               UserProfileMapper profileMapper,
                               BizDocumentMapper documentMapper,
                               BizKnowledgeMetaMapper knowledgeMapper,
                               RedisCache redisCache) {
        this.userMapper = userMapper;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.profileMapper = profileMapper;
        this.documentMapper = documentMapper;
        this.knowledgeMapper = knowledgeMapper;
        this.redisCache = redisCache;
    }

    /**
     * 获取首页统计数据
     */
    @Operation(summary = "首页统计", description = "获取系统概览统计数据")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/stats")
    public AjaxResult stats() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 用户总数
        data.put("userCount", userMapper.selectUserCount());

        // 在线用户数
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        data.put("onlineCount", keys != null ? keys.size() : 0);

        // 问诊会话总数
        data.put("sessionCount", sessionMapper.selectCount(new QueryWrapper<>()));

        // 消息总数
        data.put("messageCount", messageMapper.selectCount(new QueryWrapper<>()));

        // 健康档案数
        data.put("profileCount", profileMapper.selectCount(new QueryWrapper<>()));

        // 知识库条目数
        data.put("knowledgeCount", knowledgeMapper.selectCount(new QueryWrapper<>()));

        // 文档数
        data.put("documentCount", documentMapper.selectCount(new QueryWrapper<>()));

        // 今日新增会话数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        LambdaQueryWrapper<com.axzl.biz.domain.ChatSession> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(com.axzl.biz.domain.ChatSession::getCreateTime, today);
        data.put("todaySessions", sessionMapper.selectCount(todayWrapper));

        return AjaxResult.success(data);
    }
}

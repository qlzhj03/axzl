package com.axzl.biz.consultation.service.impl;

import com.axzl.biz.consultation.domain.ChatSession;
import com.axzl.biz.consultation.mapper.ChatSessionMapper;
import com.axzl.biz.consultation.service.IChatSessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 问诊会话 服务层实现
 *
 * @author liuchao
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
        implements IChatSessionService {

    @Override
    public List<ChatSession> selectSessionList(ChatSession session) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        if (session.getUserId() != null) {
            wrapper.eq(ChatSession::getUserId, session.getUserId());
        }
        if (session.getTitle() != null && !session.getTitle().isEmpty()) {
            wrapper.like(ChatSession::getTitle, session.getTitle());
        }
        if (session.getStatus() != null && !session.getStatus().isEmpty()) {
            wrapper.eq(ChatSession::getStatus, session.getStatus());
        }
        wrapper.orderByDesc(ChatSession::getLastMessageTime);
        return list(wrapper);
    }

    @Override
    public ChatSession selectSessionById(String sessionId) {
        return getById(sessionId);
    }

    @Override
    public ChatSession selectSessionByUserIdAndSessionId(Long userId, String sessionId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
               .eq(ChatSession::getSessionId, sessionId);
        return getOne(wrapper);
    }

    @Override
    public int insertSession(ChatSession session) {
        if (session.getCreateTime() == null) {
            session.setCreateTime(new Date());
        }
        session.setUpdateTime(new Date());
        return save(session) ? 1 : 0;
    }

    @Override
    public int updateSession(ChatSession session) {
        session.setUpdateTime(new Date());
        return updateById(session) ? 1 : 0;
    }

    @Override
    public int deleteSessionByIds(String[] sessionIds) {
        int count = 0;
        for (String id : sessionIds) {
            if (removeById(id)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int updateSatisfaction(String sessionId, Integer satisfaction) {
        LambdaUpdateWrapper<ChatSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatSession::getSessionId, sessionId)
               .set(ChatSession::getSatisfaction, satisfaction)
               .set(ChatSession::getUpdateTime, new Date());
        return update(wrapper) ? 1 : 0;
    }
}

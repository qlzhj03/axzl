package com.axzl.biz.service.impl;

import com.axzl.biz.domain.ChatMessage;
import com.axzl.biz.mapper.ChatMessageMapper;
import com.axzl.biz.service.IChatMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 对话消息 服务层实现
 *
 * @author liuchao
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements IChatMessageService {

    @Override
    public List<ChatMessage> selectMessagesBySessionId(String sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByAsc(ChatMessage::getCreateTime);
        return list(wrapper);
    }

    @Override
    public int insertMessage(ChatMessage message) {
        if (message.getCreateTime() == null) {
            message.setCreateTime(new Date());
        }
        return save(message) ? 1 : 0;
    }

    @Override
    public List<ChatMessage> selectRecentMessages(String sessionId, int limit) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByDesc(ChatMessage::getCreateTime)
               .last("limit " + limit);
        return list(wrapper);
    }
}

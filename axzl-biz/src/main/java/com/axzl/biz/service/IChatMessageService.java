package com.axzl.biz.service;

import com.axzl.biz.domain.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 对话消息 服务层
 *
 * @author liuchao
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 查询会话的消息列表
     *
     * @param sessionId 会话ID
     * @return 消息集合
     */
    List<ChatMessage> selectMessagesBySessionId(String sessionId);

    /**
     * 新增消息
     *
     * @param message 消息信息
     * @return 结果
     */
    int insertMessage(ChatMessage message);

    /**
     * 查询最后N条消息
     *
     * @param sessionId 会话ID
     * @param limit 条数
     * @return 消息集合
     */
    List<ChatMessage> selectRecentMessages(String sessionId, int limit);
}

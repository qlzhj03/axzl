package com.axzl.biz.consultation.service;

import com.axzl.biz.consultation.domain.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 问诊会话 服务层
 *
 * @author liuchao
 */
public interface IChatSessionService extends IService<ChatSession> {

    /**
     * 查询会话列表
     *
     * @param session 查询条件
     * @return 会话集合
     */
    List<ChatSession> selectSessionList(ChatSession session);

    /**
     * 通过ID查询会话
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    ChatSession selectSessionById(String sessionId);

    /**
     * 通过用户ID和会话ID查询会话
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 会话信息
     */
    ChatSession selectSessionByUserIdAndSessionId(Long userId, String sessionId);

    /**
     * 新增会话
     *
     * @param session 会话信息
     * @return 结果
     */
    int insertSession(ChatSession session);

    /**
     * 修改会话
     *
     * @param session 会话信息
     * @return 结果
     */
    int updateSession(ChatSession session);

    /**
     * 批量删除会话
     *
     * @param sessionIds 需要删除的会话ID
     * @return 结果
     */
    int deleteSessionByIds(String[] sessionIds);

    /**
     * 更新满意度
     *
     * @param sessionId 会话ID
     * @param satisfaction 满意度
     * @return 结果
     */
    int updateSatisfaction(String sessionId, Integer satisfaction);
}

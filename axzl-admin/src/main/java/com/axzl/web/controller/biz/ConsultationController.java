package com.axzl.web.controller.biz;


import com.axzl.biz.domain.ChatSession;
import com.axzl.biz.domain.dto.ChatRequest;
import com.axzl.biz.domain.dto.SatisfactionRequest;
import com.axzl.biz.domain.vo.ChatSessionDetail;
import com.axzl.biz.service.IAiConsultationService;
import com.axzl.biz.service.IChatMessageService;
import com.axzl.biz.service.IChatSessionService;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.domain.entity.SysUser;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import com.axzl.system.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 智能问诊Controller
 *
 * @author liuchao
 */
@Tag(name = "智能问诊", description = "智能问诊相关接口")
@RestController
@RequestMapping("/biz/consultation")
public class ConsultationController extends BaseController {

    @Autowired
    private IChatSessionService sessionService;

    @Autowired
    private IChatMessageService messageService;

    @Autowired
    private IAiConsultationService aiConsultationService;

    @Autowired
    private SysUserMapper sysUserMapper;

    // 创建线程池用于异步处理SSE
    private static final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    /**
     * 查询问诊会话列表
     */
    @Operation(summary = "查询问诊会话列表", description = "分页查询问诊会话列表，支持按用户ID、标题、状态等条件搜索")
    @PreAuthorize("@ss.hasPermi('biz:consultation:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChatSession session) {
        startPage();
        List<ChatSession> list = sessionService.selectSessionList(session);
        // 批量填充用户昵称
        fillNickName(list);
        return getDataTable(list);
    }

    /**
     * 批量填充会话列表中的用户昵称
     */
    private void fillNickName(List<ChatSession> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> userIds = list.stream()
                .map(ChatSession::getUserId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }
        List<SysUser> users = sysUserMapper.selectUserByIds(userIds);
        Map<Long, String> nickNameMap = users.stream()
                .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName, (a, b) -> a));
        list.forEach(s -> s.setNickName(nickNameMap.getOrDefault(s.getUserId(), "未知用户")));
    }

    /**
     * 获取问诊会话详细信息
     */
    @Operation(summary = "获取问诊会话详情", description = "根据会话ID获取会话的详细信息")
    @PreAuthorize("@ss.hasPermi('biz:consultation:query')")
    @GetMapping(value = "/{sessionId}")
    public AjaxResult getInfo(
            @Parameter(description = "会话ID", required = true) @PathVariable String sessionId) {
        ChatSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return error("会话不存在");
        }
        return success(session);
    }


    /**
     * 智能问诊对话（用户端）
     */
    @Operation(summary = "智能问诊对话", description = "非流式对话接口，返回完整响应")
    @PostMapping("/chat")
    public AjaxResult chat(@Valid @RequestBody ChatRequest request) {

        Long userId = getUserId();
        String sessionId = request.getSessionId();

        String response = aiConsultationService.chat(userId, sessionId, request.getContent(), request.isUseKnowledgeBase());

        return success(response);
    }

    /**
     * 智能问诊流式对话（SSE）
     */
    @Operation(summary = "智能问诊流式对话", description = "基于SSE的流式对话接口，实时返回AI响应")
    @PreAuthorize("@ss.hasPermi('biz:consultation:chat')")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request) {

        Long userId = getUserId();
        String sessionId = request.getSessionId();

        // 创建SSE emitter，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300_000L);

        // 注册完成和错误回调，确保资源正确释放
        emitter.onCompletion(() -> logger.info("SSE连接已完成 - 会话: {}", sessionId));

        emitter.onTimeout(() -> {
            logger.warn("SSE连接超时 - 会话: {}", sessionId);
            emitter.complete();
        });

        emitter.onError((throwable) -> {
            // 客户端断开连接是正常行为，降级为warn日志
            if (isClientDisconnect(throwable)) {
                logger.warn("SSE客户端断开连接 - 会话: {}", sessionId);
            } else {
                logger.error("SSE连接错误 - 会话: {}", sessionId, throwable);
            }
        });

        // 在线程池中异步处理流式响应
        sseExecutor.execute(() -> {
            final Logger log = LoggerFactory.getLogger(this.getClass());
            // 标记客户端是否已断开
            final boolean[] clientDisconnected = {false};

            try {
                // 调用AI服务获取流式响应（服务内部已启动独立订阅确保数据保存）
                Flux<String> responseFlux = aiConsultationService.chatStream(userId, sessionId, request.getContent(), request.isUseKnowledgeBase());

                // 订阅流式响应并发送到前端
                responseFlux.subscribe(chunk -> {
                    if (clientDisconnected[0]) {
                        return; // 客户端已断开，跳过发送
                    }
                    try {
                        emitter.send(chunk);
                    } catch (Exception e) {
                        if (isClientDisconnect(e)) {
                            clientDisconnected[0] = true;
                            log.debug("SSE发送数据时客户端已断开 - 会话: {}", sessionId);
                        } else {
                            log.error("发送SSE数据失败 - 会话: {}", sessionId, e);
                        }
                    }
                }, error -> {
                    if (isClientDisconnect(error)) {
                        clientDisconnected[0] = true;
                        log.debug("SSE流式响应客户端已断开 - 会话: {}", sessionId);
                    } else {
                        log.error("流式响应出错 - 会话: {}", sessionId, error);
                        if (!clientDisconnected[0]) {
                            try {
                                emitter.send(SseEmitter.event().name("error").data("AI服务错误: " + error.getMessage()));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    try {
                        emitter.complete();
                    } catch (Exception ignored) {
                    }
                }, () -> {
                    if (clientDisconnected[0]) {
                        return; // 客户端已断开，跳过发送
                    }
                    // 流完成时发送结束事件
                    try {
                        emitter.send(SseEmitter.event().name("end").data("[DONE]"));
                        emitter.complete();
                    } catch (Exception e) {
                        if (isClientDisconnect(e)) {
                            clientDisconnected[0] = true;
                        } else {
                            log.error("发送SSE结束事件失败 - 会话: {}", sessionId, e);
                        }
                    }
                });
            } catch (Exception e) {
                log.error("启动流式聊天失败 - 会话: {}", sessionId, e);
                if (!clientDisconnected[0]) {
                    try {
                        emitter.send(SseEmitter.event().name("error").data("启动流式聊天失败: " + e.getMessage()));
                    } catch (Exception ignored) {
                    }
                }
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                }
            }
        });

        return emitter;
    }


    /**
     * 问诊历史
     */
    @Operation(summary = "问诊历史记录", description = "获取当前用户的问诊会话历史列表")
    @PreAuthorize("@ss.hasPermi('biz:consultation:history')")
    @GetMapping("/history")
    public TableDataInfo history() {
        startPage();
        ChatSession query = new ChatSession();
        query.setUserId(getUserId());
        return getDataTable(sessionService.selectSessionList(query));
    }

    /**
     * 会话详情
     */
    @Operation(summary = "会话详情", description = "获取指定会话的详细信息，包含会话信息和消息列表")
    @PreAuthorize("@ss.hasPermi('biz:consultation:detail')")
    @GetMapping("/detail/{sessionId}")
    public AjaxResult detail(
            @Parameter(description = "会话ID", required = true) @PathVariable String sessionId) {
        ChatSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return error("会话不存在");
        }
        // 验证归属
        if (!session.getUserId().equals(getUserId())) {
            return error("无权查看该会话");
        }
        ChatSessionDetail detail = new ChatSessionDetail();
        detail.setSession(session);
        detail.setMessages(messageService.selectMessagesBySessionId(sessionId));
        return success(detail);
    }

    /**
     * 管理端会话详情（含分页消息）
     */
    @Operation(summary = "管理端会话详情", description = "管理端获取指定会话的详细信息，含分页消息列表")
    @PreAuthorize("@ss.hasPermi('biz:consultation:query')")
    @GetMapping("/detail/admin/{sessionId}")
    public AjaxResult adminDetail(
            @Parameter(description = "会话ID", required = true) @PathVariable String sessionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {
        ChatSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return error("会话不存在");
        }
        // 填充用户昵称
        if (session.getUserId() != null) {
            SysUser user = sysUserMapper.selectUserById(session.getUserId());
            if (user != null) {
                session.setNickName(user.getNickName());
            }
        }
        ChatSessionDetail detail = new ChatSessionDetail();
        detail.setSession(session);
        int offset = (pageNum - 1) * pageSize;
        detail.setMessages(messageService.selectMessagesPaged(sessionId, offset, pageSize));
        return success(detail);
    }

    /**
     * 删除会话
     */
    @Operation(summary = "删除会话", description = "删除指定的问诊会话")
    @PreAuthorize("@ss.hasPermi('biz:consultation:remove')")
    @Log(title = "问诊记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/remove/{sessionId}")
    public AjaxResult removeUserSession(
            @Parameter(description = "会话ID", required = true) @PathVariable String sessionId) {
        ChatSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return error("会话不存在");
        }
        if (!session.getUserId().equals(getUserId())) {
            return error("无权删除该会话");
        }
        String[] ids = {sessionId};
        return toAjax(sessionService.deleteSessionByIds(ids));
    }

    /**
     * 批量删除问诊会话
     */
    @Operation(summary = "批量删除问诊会话", description = "批量删除指定的问诊会话")
    @PreAuthorize("@ss.hasPermi('biz:consultation:remove')")
    @Log(title = "问诊记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sessionIds}")
    public AjaxResult remove(
            @Parameter(description = "会话ID数组", required = true) @PathVariable String[] sessionIds) {
        return toAjax(sessionService.deleteSessionByIds(sessionIds));
    }

    /**
     * 满意度评价（用户端）
     */
    @Operation(summary = "满意度评价", description = "对问诊会话进行满意度评分（1-5分）")
    @PreAuthorize("@ss.hasPermi('biz:consultation:satisfaction')")
    @Log(title = "问诊满意度评价", businessType = BusinessType.UPDATE)
    @PutMapping("/satisfaction/{sessionId}")
    public AjaxResult satisfaction(
            @Parameter(description = "会话ID", required = true) @PathVariable String sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "满意度评价请求", required = true) 
            @RequestBody SatisfactionRequest request) {
        ChatSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return error("会话不存在");
        }
        if (request.getSatisfaction() == null || request.getSatisfaction() < 1 || request.getSatisfaction() > 5) {
            return error("满意度评分必须在1-5之间");
        }
        return toAjax(sessionService.updateSatisfaction(sessionId, request.getSatisfaction()));
    }

    /**
     * 判断是否为客户端断开连接异常
     */
    private boolean isClientDisconnect(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        String message = throwable.getMessage();
        if (message != null && (message.contains("你的主机中的软件中止了一个已建立的连接")
                || message.contains("Connection reset by peer")
                || message.contains("Broken pipe")
                || message.contains("AsyncRequestNotUsableException"))) {
            return true;
        }
        String className = throwable.getClass().getSimpleName();
        return className.contains("ClientAbort") || className.contains("AsyncRequestNotUsable");
    }

    /**
     * 修改会话标题
     */
    @Operation(summary = "修改会话标题", description = "修改指定会话的标题")
    @PreAuthorize("@ss.hasPermi('biz:consultation:edit')")
    @Log(title = "修改会话标题", businessType = BusinessType.UPDATE)
    @PutMapping("/title/{sessionId}")
    public AjaxResult updateTitle(
            @Parameter(description = "会话ID", required = true) @PathVariable String sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "新标题", required = true)
            @RequestBody String title) {
        ChatSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return error("会话不存在");
        }
        // 验证归属
        if (!session.getUserId().equals(getUserId())) {
            return error("无权修改该会话");
        }
        return toAjax(sessionService.updateTitle(sessionId, title));
    }

    /**
     * 置顶/取消置顶会话
     */
    @Operation(summary = "置顶/取消置顶会话", description = "对指定会话进行置顶或取消置顶操作")
    @PreAuthorize("@ss.hasPermi('biz:consultation:pin')")
    @Log(title = "置顶会话", businessType = BusinessType.UPDATE)
    @PutMapping("/pin/{sessionId}")
    public AjaxResult updatePinStatus(
            @Parameter(description = "会话ID", required = true) @PathVariable String sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "是否置顶(0否 1是)", required = true)
            @RequestBody Integer isPinned) {
        ChatSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return error("会话不存在");
        }
        // 验证归属
        if (!session.getUserId().equals(getUserId())) {
            return error("无权操作该会话");
        }
        if (isPinned == null || (isPinned != 0 && isPinned != 1)) {
            return error("置顶状态参数错误");
        }
        return toAjax(sessionService.updatePinStatus(sessionId, isPinned));
    }
}

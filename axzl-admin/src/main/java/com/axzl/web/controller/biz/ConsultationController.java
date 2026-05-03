package com.axzl.web.controller.biz;


import com.axzl.biz.service.AiConsultationService;
import com.axzl.biz.domain.ChatSession;
import com.axzl.biz.domain.dto.ChatRequest;
import com.axzl.biz.domain.dto.SatisfactionRequest;
import com.axzl.biz.domain.vo.ChatSessionDetail;
import com.axzl.biz.service.IChatMessageService;
import com.axzl.biz.service.IChatSessionService;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 智能问诊Controller（统一管理端和用户端）
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
    private AiConsultationService aiConsultationService;

    // 创建线程池用于异步处理SSE
    private static final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    /**
     * 查询问诊会话列表
     */
    @Operation(summary = "查询问诊会话列表", description = "分页查询问诊会话列表，支持按用户昵称、标题、状态等条件搜索")
    @PreAuthorize("@ss.hasPermi('biz:consultation:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChatSession session) {
        startPage();
        List<ChatSession> list = sessionService.selectSessionList(session);
        return getDataTable(list);
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

        String response = aiConsultationService.chat(userId, sessionId, request.getContent());

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
        emitter.onCompletion(() -> {
            logger.info("SSE连接已完成 - 会话: {}", sessionId);
        });

        emitter.onTimeout(() -> {
            logger.warn("SSE连接超时 - 会话: {}", sessionId);
            emitter.complete();
        });

        emitter.onError((throwable) -> {
            logger.error("SSE连接错误 - 会话: {}", sessionId, throwable);
        });

        // 在线程池中异步处理流式响应
        sseExecutor.execute(() -> {
            final Logger log = LoggerFactory.getLogger(this.getClass());
            try {

                // 调用AI服务获取流式响应
                Flux<String> responseFlux = aiConsultationService.chatStream(userId, sessionId, request.getContent());

                // 订阅流式响应并发送到前端
                responseFlux.subscribe(chunk -> {
                    try {
                        // 直接发送原始文本块，Spring会自动处理SSE格式
                        // 文本中的\n会被保留在data值中
                        emitter.send(chunk);
                    } catch (IOException e) {
                        log.error("发送SSE数据失败", e);
                        emitter.completeWithError(e);
                    }
                }, error -> {
                    log.error("流式响应出错", error);
                    try {
                        emitter.send(SseEmitter.event().name("error").data("AI服务错误: " + error.getMessage()));
                    } catch (IOException e) {
                        log.error("发送SSE错误事件失败", e);
                    }
                    emitter.completeWithError(error);
                }, () -> {
                    // 流完成时发送结束事件
                    try {
                        emitter.send(SseEmitter.event().name("end").data("[DONE]"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送SSE结束事件失败", e);
                        emitter.completeWithError(e);
                    }
                });
            } catch (Exception e) {
                log.error("启动流式聊天失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("启动流式聊天失败: " + e.getMessage()));
                } catch (IOException ex) {
                    log.error("发送SSE错误事件失败", ex);
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }


    /**
     * 问诊历史（用户端，分页）
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
     * 会话详情（用户端）
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
     * 删除会话（用户端）
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
     * 修改会话标题（用户端）
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
     * 置顶/取消置顶会话（用户端）
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

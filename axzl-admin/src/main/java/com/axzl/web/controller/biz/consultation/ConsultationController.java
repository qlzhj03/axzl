package com.axzl.web.controller.biz.consultation;

import com.axzl.biz.ai.service.AiConsultationService;
import com.axzl.biz.consultation.domain.ChatMessage;
import com.axzl.biz.consultation.domain.ChatSession;
import com.axzl.biz.consultation.domain.dto.ChatRequest;
import com.axzl.biz.consultation.domain.dto.SatisfactionRequest;
import com.axzl.biz.consultation.service.IChatMessageService;
import com.axzl.biz.consultation.service.IChatSessionService;
import com.axzl.biz.consultation.domain.vo.ChatResponse;
import com.axzl.biz.consultation.domain.vo.ChatSessionDetail;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 智能问诊Controller（统一管理端和用户端）
 *
 * @author liuchao
 */
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
     * 智能问诊对话（用户端）
     */
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
                responseFlux.subscribe(
                        chunk -> {
                            try {
                                // 直接发送原始文本块，Spring会自动处理SSE格式
                                // 文本中的\n会被保留在data值中
                                emitter.send(chunk);
                            } catch (IOException e) {
                                log.error("发送SSE数据失败", e);
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("流式响应出错", error);
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("AI服务错误: " + error.getMessage()));
                            } catch (IOException e) {
                                log.error("发送SSE错误事件失败", e);
                            }
                            emitter.completeWithError(error);
                        },
                        () -> {
                            // 流完成时发送结束事件
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("end")
                                        .data("[DONE]"));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("发送SSE结束事件失败", e);
                                emitter.completeWithError(e);
                            }
                        }
                );
            } catch (Exception e) {
                log.error("启动流式聊天失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("启动流式聊天失败: " + e.getMessage()));
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
    @PreAuthorize("@ss.hasPermi('biz:consultation:detail')")
    @GetMapping("/detail/{sessionId}")
    public AjaxResult detail(@PathVariable String sessionId) {
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
    @PreAuthorize("@ss.hasPermi('biz:consultation:remove')")
    @Log(title = "问诊记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/remove/{sessionId}")
    public AjaxResult removeUserSession(@PathVariable String sessionId) {
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
     * 满意度评价（用户端）
     */
    @PreAuthorize("@ss.hasPermi('biz:consultation:satisfaction')")
    @Log(title = "问诊满意度评价", businessType = BusinessType.UPDATE)
    @PutMapping("/satisfaction/{sessionId}")
    public AjaxResult satisfaction(@PathVariable String sessionId,
                                   @RequestBody SatisfactionRequest request) {
        ChatSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return error("会话不存在");
        }
        if (request.getSatisfaction() == null
                || request.getSatisfaction() < 1
                || request.getSatisfaction() > 5) {
            return error("满意度评分必须在1-5之间");
        }
        return toAjax(sessionService.updateSatisfaction(sessionId, request.getSatisfaction()));
    }
}

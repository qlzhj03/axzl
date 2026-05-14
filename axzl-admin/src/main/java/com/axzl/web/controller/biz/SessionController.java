package com.axzl.web.controller.biz;

import com.axzl.biz.domain.ChatSession;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话管理Controller
 *
 * @author liuchao
 */
@Tag(name = "会话管理", description = "问诊会话管理相关接口")
@RestController
@RequestMapping("/biz/session")
public class SessionController extends BaseController {

    private final IChatSessionService sessionService;
    private final IChatMessageService messageService;

    public SessionController(IChatSessionService sessionService, IChatMessageService messageService) {
        this.sessionService = sessionService;
        this.messageService = messageService;
    }


    /**
     * 查询会话列表（管理端）
     */
    @Operation(summary = "查询会话列表", description = "分页查询问诊会话列表")
    @PreAuthorize("@ss.hasPermi('biz:consultation:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChatSession session) {
        startPage();
        List<ChatSession> list = sessionService.selectSessionList(session);
        return getDataTable(list);
    }

    /**
     * 删除会话（管理端）
     */
    @Operation(summary = "删除会话", description = "批量删除问诊会话")
    @PreAuthorize("@ss.hasPermi('biz:consultation:remove')")
    @Log(title = "问诊记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sessionIds}")
    public AjaxResult remove(
            @Parameter(description = "会话ID数组", required = true) @PathVariable String[] sessionIds) {
        return toAjax(sessionService.deleteSessionByIds(sessionIds));
    }

    /**
     * 重命名会话
     */
    @Operation(summary = "重命名会话", description = "修改问诊会话名称")
    @PreAuthorize("@ss.hasPermi('biz:consultation:rename')")
    @Log(title = "修改问诊记录", businessType = BusinessType.UPDATE)
    @PutMapping("/rename")
    public AjaxResult rename(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "会话信息（包含sessionId和title）", required = true) 
            ChatSession session) {
        return toAjax(sessionService.updateSession(session));
    }
}

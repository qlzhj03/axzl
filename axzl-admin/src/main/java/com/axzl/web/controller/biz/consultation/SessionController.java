package com.axzl.web.controller.biz.consultation;

import com.axzl.biz.consultation.domain.ChatSession;
import com.axzl.biz.consultation.service.IChatMessageService;
import com.axzl.biz.consultation.service.IChatSessionService;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

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
    @PreAuthorize("@ss.hasPermi('biz:consultation:remove')")
    @Log(title = "问诊记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sessionIds}")
    public AjaxResult remove(@PathVariable String[] sessionIds) {
        return toAjax(sessionService.deleteSessionByIds(sessionIds));
    }

    @PreAuthorize("@ss.hasPermi('biz:consultation:rename')")
    @Log(title = "修改问诊记录", businessType = BusinessType.UPDATE)
    @PutMapping("/rename")
    public AjaxResult rename(ChatSession session) {
        return toAjax(sessionService.updateSession(session));
    }
}

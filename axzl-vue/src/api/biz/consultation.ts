import request from '@/utils/request'
import { getToken } from '@/utils/auth'
import { fetchEventSource, EventSourceMessage } from '@microsoft/fetch-event-source'

// 查询问诊会话列表
export function listConsultation(query: any) {
  return request({
    url: '/biz/consultation/list',
    method: 'get',
    params: query
  })
}

// 查询问诊会话详细
export function getConsultation(sessionId: string) {
  return request({
    url: '/biz/consultation/' + sessionId,
    method: 'get'
  })
}

// 删除问诊会话
export function delConsultation(sessionIds: string | string[]) {
  return request({
    url: '/biz/consultation/' + sessionIds,
    method: 'delete'
  })
}

// 查询会话消息列表
export function getSessionMessages(sessionId: string) {
  return request({
    url: '/biz/consultation/' + sessionId,
    method: 'get'
  })
}

// 管理端获取会话详情（含分页消息）
export function getAdminSessionDetail(sessionId: string, pageNum?: number, pageSize?: number) {
  return request({
    url: '/biz/consultation/detail/admin/' + sessionId,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export interface StreamCallbacks {
  onMessage: (text: string) => void
  onSessionId?: (sessionId: string) => void
  onDone?: () => void
  onError?: (error: Error) => void
}

// 智能问诊流式对话（SSE）
export function chatStream(data: any, callbacks: StreamCallbacks): AbortController {
  const controller = new AbortController()
  const token = getToken() || ''

  fetchEventSource('/dev-api/biz/consultation/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token,
    },
    body: JSON.stringify(data),
    signal: controller.signal,
    openWhenHidden: true,
    onmessage(msg: EventSourceMessage) {
      if (msg.event === 'end') {
        callbacks.onDone?.()
        controller.abort()
        return
      }
      if (msg.event === 'error') {
        callbacks.onError?.(new Error(msg.data))
        controller.abort()
        return
      }
      try {
        const obj = JSON.parse(msg.data)
        if (obj.sessionId) {
          callbacks.onSessionId?.(obj.sessionId)
          return
        }
      } catch {}
      callbacks.onMessage(msg.data)
    },
    onclose() {
      callbacks.onDone?.()
    },
    onerror(err) {
      callbacks.onError?.(err)
      throw err
    }
  })

  return controller
}

// 修改会话标题
export function updateConsultationTitle(sessionId: string, title: string) {
  return request({
    url: '/biz/consultation/title/' + sessionId,
    method: 'put',
    data: title
  })
}

// 置顶/取消置顶会话
export function updatePinStatus(sessionId: string, isPinned: number) {
  return request({
    url: '/biz/consultation/pin/' + sessionId,
    method: 'put',
    data: isPinned
  })
}

// --- 患者端专属接口 ---

// 我的问诊历史（分页）
export function getMyHistory(query?: { pageNum?: number; pageSize?: number }) {
  return request({
    url: '/biz/consultation/history',
    method: 'get',
    params: query
  })
}

// 会话详情（含全部消息，患者端）
export function getSessionDetail(sessionId: string) {
  return request({
    url: '/biz/consultation/detail/' + sessionId,
    method: 'get'
  })
}

// 删除我的会话
export function deleteMySession(sessionId: string) {
  return request({
    url: '/biz/consultation/remove/' + sessionId,
    method: 'delete'
  })
}

// 评价会话满意度
export function rateSession(sessionId: string, satisfaction: number) {
  return request({
    url: '/biz/consultation/satisfaction/' + sessionId,
    method: 'put',
    data: { satisfaction }
  })
}

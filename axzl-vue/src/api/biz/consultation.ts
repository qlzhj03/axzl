import request from '@/utils/request'
import { getToken } from '@/utils/auth'

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

// 智能问诊流式对话（SSE）
export function chatStream(data: any) {
  // 获取token
  const token = getToken() || ''
  
  return fetch('/dev-api/biz/consultation/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token,
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify(data)
  })
}

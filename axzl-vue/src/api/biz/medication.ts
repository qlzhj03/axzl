import request from '@/utils/request'

// 获取我的用药列表
export function getMyMedications(params?: any) {
  return request({
    url: '/biz/medication/my',
    method: 'get',
    params
  })
}

// 获取今日用药提醒 + 打卡记录
export function getTodayMedications() {
  return request({
    url: '/biz/medication/my/today',
    method: 'get'
  })
}

// 新增用药
export function addMedication(data: any) {
  return request({
    url: '/biz/medication/my',
    method: 'post',
    data
  })
}

// 修改用药
export function updateMedication(data: any) {
  return request({
    url: '/biz/medication/my',
    method: 'put',
    data
  })
}

// 删除用药
export function delMedication(ids: number | number[]) {
  return request({
    url: '/biz/medication/my/' + ids,
    method: 'delete'
  })
}

// 用药打卡
export function checkInMedication(data: { medicationId: number; scheduledTime?: string }) {
  return request({
    url: '/biz/medication/my/checkin',
    method: 'post',
    data
  })
}

// 获取今日打卡记录
export function getTodayLogs() {
  return request({
    url: '/biz/medication/my/logs',
    method: 'get'
  })
}

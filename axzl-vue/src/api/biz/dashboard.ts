import request from '@/utils/request'

// 获取首页统计数据
export function getDashboardStats() {
  return request({
    url: '/biz/dashboard/stats',
    method: 'get'
  })
}

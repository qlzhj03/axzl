import request from '@/utils/request'
import type { ProfileQueryParams, ProfileFormData, UserProfile, AjaxResult, TableDataInfo } from '@/types/api/biz/profile'

// 查询健康档案列表
export function listProfile(query: ProfileQueryParams): Promise<TableDataInfo<UserProfile[]>> {
  return request({
    url: '/biz/profile/list',
    method: 'get',
    params: query
  })
}

// 查询健康档案详细
export function getProfile(profileId: number): Promise<AjaxResult> {
  return request({
    url: '/biz/profile/' + profileId,
    method: 'get'
  })
}

// 新增健康档案
export function addProfile(data: ProfileFormData): Promise<AjaxResult> {
  return request({
    url: '/biz/profile',
    method: 'post',
    data: data
  })
}

// 修改健康档案
export function updateProfile(data: ProfileFormData): Promise<AjaxResult> {
  return request({
    url: '/biz/profile',
    method: 'put',
    data: data
  })
}

// 删除健康档案
export function delProfile(profileIds: number | number[]): Promise<AjaxResult> {
  return request({
    url: '/biz/profile/' + profileIds,
    method: 'delete'
  })
}

import request from '@/utils/request'
import type { ProfileQueryParams, ProfileFormData, UserProfile} from '@/types/api/biz/profile'
import {AjaxResult, TableDataInfo} from "@/types";

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

// 导出健康档案
export function exportProfile(query: ProfileQueryParams): Promise<any> {
  return request({
    url: '/biz/profile/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 下载健康档案导入模板
export function importTemplate(): Promise<any> {
  return request({
    url: '/biz/profile/importTemplate',
    method: 'post',
    responseType: 'blob'
  })
}

// 导入健康档案数据
export function importData(data: FormData): Promise<AjaxResult> {
  return request({
    url: '/biz/profile/importData',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// --- 患者端专属接口 ---

// 获取我的健康档案（不存在则自动创建）
export function getMyProfile(): Promise<AjaxResult> {
  return request({
    url: '/biz/profile/my',
    method: 'get'
  })
}

// 创建或更新我的健康档案
export function updateMyProfile(data: any): Promise<AjaxResult> {
  return request({
    url: '/biz/profile/my',
    method: 'post',
    data: data
  })
}

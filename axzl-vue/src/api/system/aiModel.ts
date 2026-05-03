import request from '@/utils/request'
import type { AIModelConfig, AIModelQueryParams } from '@/types/api/system/aiModel'
import type { AjaxResult, TableDataInfo } from '@/types/api/common'

// 查询AI大模型配置列表
export function listAiModel(query: AIModelQueryParams) {
  return request({
    url: '/system/aiModel/list',
    method: 'get',
    params: query
  }) as Promise<TableDataInfo<AIModelConfig[]>>
}

// 查询AI大模型配置详细
export function getAiModel(id: number | string) {
  return request({
    url: '/system/aiModel/' + id,
    method: 'get'
  }) as Promise<AjaxResult<AIModelConfig>>
}

// 根据模型名称获取配置
export function getAiModelByName(modelName: string) {
  return request({
    url: '/system/aiModel/name/' + modelName,
    method: 'get'
  }) as Promise<AjaxResult<AIModelConfig>>
}

// 获取默认模型配置
export function getDefaultModel(modelType: string) {
  return request({
    url: '/system/aiModel/default/' + modelType,
    method: 'get'
  }) as Promise<AjaxResult<AIModelConfig>>
}

// 新增AI大模型配置
export function addAiModel(data: AIModelConfig) {
  return request({
    url: '/system/aiModel',
    method: 'post',
    data: data
  }) as Promise<AjaxResult>
}

// 修改AI大模型配置
export function updateAiModel(data: AIModelConfig) {
  return request({
    url: '/system/aiModel',
    method: 'put',
    data: data
  }) as Promise<AjaxResult>
}

// 切换默认模型
export function switchDefaultModel(id: number | string) {
  return request({
    url: '/system/aiModel/switchDefault/' + id,
    method: 'put'
  }) as Promise<AjaxResult>
}

// 删除AI大模型配置
export function delAiModel(id: number | string) {
  return request({
    url: '/system/aiModel/' + id,
    method: 'delete'
  }) as Promise<AjaxResult>
}

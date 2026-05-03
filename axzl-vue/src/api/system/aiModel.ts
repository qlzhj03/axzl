import request from '@/utils/request'

// 查询AI大模型配置列表
export function listAiModel(query: any) {
  return request({
    url: '/system/aiModel/list',
    method: 'get',
    params: query
  })
}

// 查询AI大模型配置详细
export function getAiModel(modelId: number | string) {
  return request({
    url: '/system/aiModel/' + modelId,
    method: 'get'
  })
}

// 根据模型编码获取配置
export function getAiModelByCode(modelCode: string) {
  return request({
    url: '/system/aiModel/code/' + modelCode,
    method: 'get'
  })
}

// 获取默认模型配置
export function getDefaultModel(modelType: string) {
  return request({
    url: '/system/aiModel/default/' + modelType,
    method: 'get'
  })
}

// 新增AI大模型配置
export function addAiModel(data: any) {
  return request({
    url: '/system/aiModel',
    method: 'post',
    data: data
  })
}

// 修改AI大模型配置
export function updateAiModel(data: any) {
  return request({
    url: '/system/aiModel',
    method: 'put',
    data: data
  })
}

// 切换默认模型
export function switchDefaultModel(modelId: number | string) {
  return request({
    url: '/system/aiModel/switchDefault/' + modelId,
    method: 'put'
  })
}

// 删除AI大模型配置
export function delAiModel(modelId: number | string) {
  return request({
    url: '/system/aiModel/' + modelId,
    method: 'delete'
  })
}

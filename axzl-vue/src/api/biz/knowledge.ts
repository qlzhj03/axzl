import request from '@/utils/request'

// 查询知识库列表（管理端）
export function listKnowledge(query: any) {
  return request({
    url: '/biz/knowledge/admin/list',
    method: 'get',
    params: query
  })
}

// 查询知识详情
export function getKnowledge(id: number) {
  return request({
    url: '/biz/knowledge/admin/' + id,
    method: 'get'
  })
}

// 新增知识
export function addKnowledge(data: any) {
  return request({
    url: '/biz/knowledge/admin',
    method: 'post',
    data: data
  })
}

// 修改知识
export function updateKnowledge(data: any) {
  return request({
    url: '/biz/knowledge/admin',
    method: 'put',
    data: data
  })
}

// 删除知识
export function delKnowledge(ids: number[]) {
  return request({
    url: '/biz/knowledge/admin/' + ids.join(','),
    method: 'delete'
  })
}

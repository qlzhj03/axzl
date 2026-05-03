import request from '@/utils/request'

// 查询文档列表
export function listDocument(query: any) {
  return request({
    url: '/biz/knowledge/document/list',
    method: 'get',
    params: query
  })
}

// 查询文档详细
export function getDocument(id: number) {
  return request({
    url: '/biz/knowledge/document/' + id,
    method: 'get'
  })
}

// 上传文档
export function uploadDocument(data: FormData) {
  return request({
    url: '/biz/knowledge/document/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文档
export function delDocument(id: number | number[]) {
  return request({
    url: '/biz/knowledge/document/' + id,
    method: 'delete'
  })
}

// 重新处理文档（向量化）
export function reprocessDocument(id: number) {
  return request({
    url: '/biz/knowledge/document/reprocess/' + id,
    method: 'post'
  })
}

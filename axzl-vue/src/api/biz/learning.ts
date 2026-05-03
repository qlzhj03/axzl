import request from '@/utils/request'
import type { ArticleQueryParams, CategoryQueryParams, ArticleFormData, CategoryFormData, LearningArticle, LearningCategory } from '@/types/api/biz/learning'
import { AjaxResult, TableDataInfo } from '@/types'

// ==================== 分类管理 ====================

// 查询分类列表
export function listCategory(query: CategoryQueryParams): Promise<TableDataInfo<LearningCategory[]>> {
  return request({
    url: '/biz/learning/category/list',
    method: 'get',
    params: query
  })
}

// 查询分类详细
export function getCategory(categoryId: number): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/category/' + categoryId,
    method: 'get'
  })
}

// 新增分类
export function addCategory(data: CategoryFormData): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/category',
    method: 'post',
    data: data
  })
}

// 修改分类
export function updateCategory(data: CategoryFormData): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/category',
    method: 'put',
    data: data
  })
}

// 删除分类
export function delCategory(categoryIds: number | number[]): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/category/' + categoryIds,
    method: 'delete'
  })
}

// 获取分类树
export function getCategoryTree(): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/category/tree',
    method: 'get'
  })
}

// ==================== 文章管理（管理端） ====================

// 查询文章列表
export function listArticle(query: ArticleQueryParams): Promise<TableDataInfo<LearningArticle[]>> {
  return request({
    url: '/biz/learning/article/admin/list',
    method: 'get',
    params: query
  })
}

// 查询文章详细
export function getArticle(articleId: number): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/admin/' + articleId,
    method: 'get'
  })
}

// 新增文章
export function addArticle(data: ArticleFormData): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/admin',
    method: 'post',
    data: data
  })
}

// 修改文章
export function updateArticle(data: ArticleFormData): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/admin',
    method: 'put',
    data: data
  })
}

// 删除文章
export function delArticle(articleIds: number | number[]): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/admin/' + articleIds,
    method: 'delete'
  })
}

// 发布/下架文章
export function publishArticle(articleId: number): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/admin/publish/' + articleId,
    method: 'put'
  })
}

// AI生成摘要
export function generateAiSummary(articleId: number): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/admin/ai-summary/' + articleId,
    method: 'post'
  })
}

// ==================== 文章（患者端） ====================

// 查询已发布文章列表
export function getPublishedArticles(query: ArticleQueryParams): Promise<TableDataInfo<LearningArticle[]>> {
  return request({
    url: '/biz/learning/article/list',
    method: 'get',
    params: query
  })
}

// 阅读文章详情
export function readArticle(articleId: number): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/' + articleId,
    method: 'get'
  })
}

// 切换收藏
export function toggleBookmark(articleId: number): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/bookmark/' + articleId,
    method: 'post'
  })
}

// 切换点赞
export function toggleLike(articleId: number): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/like/' + articleId,
    method: 'post'
  })
}

// 更新阅读进度
export function updateReadProgress(data: { articleId: number; progress?: number; duration?: number }): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/progress',
    method: 'put',
    data: data
  })
}

// 获取收藏列表
export function getBookmarks(): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/bookmarks',
    method: 'get'
  })
}

// 获取推荐文章
export function getRecommended(): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/recommended',
    method: 'get'
  })
}

// 获取学习统计
export function getLearningStats(): Promise<AjaxResult> {
  return request({
    url: '/biz/learning/article/stats',
    method: 'get'
  })
}

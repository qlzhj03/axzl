/**
 * 健康知识学习数据类型定义
 */

// 健康知识分类
export interface LearningCategory {
  categoryId?: number
  parentId?: number
  categoryName?: string
  sortOrder?: number
  icon?: string
  status?: string
  children?: LearningCategory[]
  createTime?: string
  remark?: string
}

// 健康知识文章
export interface LearningArticle {
  articleId?: number
  categoryId?: number
  categoryName?: string
  title?: string
  summary?: string
  content?: string
  coverImage?: string
  author?: string
  source?: string
  tags?: string
  viewCount?: number
  likeCount?: number
  aiSummary?: string
  isTop?: number
  isFeatured?: number
  status?: string
  publishTime?: string
  createTime?: string
  updateTime?: string
  remark?: string
  // 患者端附加字段
  isBookmarked?: number
  readProgress?: number
}

// 用户文章互动记录
export interface UserArticleRecord {
  id?: number
  userId?: number
  articleId?: number
  isBookmarked?: number
  isLiked?: number
  readProgress?: number
  readDuration?: number
  lastReadTime?: string
  quizScore?: number
  createTime?: string
  updateTime?: string
}

// 文章查询参数
export interface ArticleQueryParams {
  pageNum?: number
  pageSize?: number
  title?: string
  categoryId?: number
  status?: string
  author?: string
  tags?: string
  beginTime?: string
  endTime?: string
}

// 分类查询参数
export interface CategoryQueryParams {
  categoryName?: string
  status?: string
  parentId?: number
}

// 文章表单数据
export type ArticleFormData = Partial<LearningArticle>

// 分类表单数据
export type CategoryFormData = Partial<LearningCategory>

// 学习统计
export interface LearningStats {
  readCount?: number
  bookmarkCount?: number
  likeCount?: number
  totalDuration?: number
}

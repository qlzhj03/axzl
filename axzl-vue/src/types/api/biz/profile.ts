/**
 * 健康档案数据类型定义
 */

// 健康档案对象
export interface UserProfile {
  profileId?: number
  userId?: number
  nickName?: string  // 用户昵称（非数据库字段，用于展示）
  birthday?: string
  gender?: string
  bloodType?: string
  height?: number
  weight?: number
  allergyHistory?: string
  medicalHistory?: string
  familyHistory?: string
  lifestyle?: string
  status?: string
  createTime?: string
  updateTime?: string
  remark?: string
}

// 健康档案查询参数
export interface ProfileQueryParams {
  pageNum?: number
  pageSize?: number
  userId?: number
  gender?: string
  status?: string
  beginTime?: string
  endTime?: string
}

// 健康档案表单数据
export type ProfileFormData = UserProfile

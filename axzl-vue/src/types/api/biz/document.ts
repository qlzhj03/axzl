/**
 * 文档信息
 */
export interface BizDocument {
  /** 文档ID */
  id?: number;
  /** 原始文件名 */
  fileName?: string;
  /** 文件类型(txt/word/pdf/md/jsonl) */
  fileType?: string;
  /** 文件大小(字节) */
  fileSize?: number;
  /** Milvus对象存储路径 */
  milvusFilePath?: string;
  /** 提取的文本内容 */
  extractedContent?: string;
  /** 切分块数 */
  chunkCount?: number;
  /** 向量状态(0未处理 1处理中 2已完成 3失败) */
  vectorStatus?: string;
  /** 上传用户ID */
  uploadBy?: number;
  /** 状态(0正常 1删除) */
  status?: string;
  /** 上传时间 */
  createTime?: string;
  /** 更新时间 */
  updateTime?: string;
  /** 备注 */
  remark?: string;
}

/**
 * 文档查询参数
 */
export interface DocumentQueryParams {
  /** 页码 */
  pageNum?: number;
  /** 每页数量 */
  pageSize?: number;
  /** 文件名 */
  fileName?: string;
  /** 文件类型 */
  fileType?: string;
  /** 向量状态 */
  vectorStatus?: string;
  /** 排序字段 */
  orderByColumn?: string;
  /** 排序方向 */
  isAsc?: string;
}

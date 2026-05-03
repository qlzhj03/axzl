import type { PageDomain, BaseEntity } from "../common";

/** AI模型配置分页查询参数 */
export interface AIModelQueryParams extends PageDomain {
  /** 模型名称 */
  modelName?: string;
  /** 提供商 */
  provider?: string;
  /** 模型类型 */
  modelType?: string;
  /** 状态 */
  status?: string;
}

/** AI模型配置信息 */
export interface AIModelConfig extends BaseEntity {
  /** 主键ID */
  id?: number;
  /** 提供商标识 */
  provider?: string;
  /** 模型名称 */
  modelName?: string;
  /** API基础URL */
  apiBaseUrl?: string;
  /** API密钥 */
  apiKey?: string;
  /** 模型类型(text/embedding/image) */
  modelType?: string;
  /** 温度参数 */
  temperature?: number;
  /** 最大Token数 */
  maxTokens?: number;
  /** Top-P参数 */
  topP?: number;
  /** Top-K参数 */
  topK?: number;
  /** 频率惩罚 */
  frequencyPenalty?: number;
  /** 存在惩罚 */
  presencePenalty?: number;
  /** 停止序列 */
  stopSequences?: string;
  /** 是否默认(0否 1是) */
  isDefault?: number;
  /** 显示排序 */
  sortOrder?: number;
  /** 额外配置JSON */
  configExtra?: string;
  /** 状态(0停用 1正常) */
  status?: number;
}

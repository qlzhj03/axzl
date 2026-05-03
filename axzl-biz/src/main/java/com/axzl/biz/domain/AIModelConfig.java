package com.axzl.biz.domain;

import com.axzl.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * AI大模型配置对象 biz_ai_model_config
 *
 * @author liuchao
 */
@TableName("biz_ai_model_config")
public class AIModelConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提供商标识(如openai/anthropic/deepseek)
     */
    @Excel(name = "提供商")
    private String provider;

    /**
     * 模型名称
     */
    @Excel(name = "模型名称")
    private String modelName;

    /**
     * API基础URL
     */
    @Excel(name = "API基础URL")
    private String apiBaseUrl;

    /**
     * API密钥(加密存储)
     */
    private String apiKey;

    /**
     * 模型类型(text/embedding/image/audio)
     */
    @Excel(name = "模型类型", readConverterExp = "text=文本,embedding=向量,image=图像,audio=音频")
    private String modelType;

    /**
     * 温度参数(0-2)
     */
    @Excel(name = "温度参数")
    private BigDecimal temperature;

    /**
     * 最大生成Token数
     */
    @Excel(name = "最大Token数")
    private Integer maxTokens;

    /**
     * Top-P采样参数
     */
    @Excel(name = "Top-P参数")
    private BigDecimal topP;

    /**
     * Top-K采样参数(0=不限制)
     */
    @Excel(name = "Top-K参数")
    private Integer topK;

    /**
     * 频率惩罚(-2.0至2.0)
     */
    @Excel(name = "频率惩罚")
    private BigDecimal frequencyPenalty;

    /**
     * 存在惩罚(-2.0至2.0)
     */
    @Excel(name = "存在惩罚")
    private BigDecimal presencePenalty;

    /**
     * 停止序列(JSON数组)
     */
    private String stopSequences;

    /**
     * 是否默认模型(0-否，1-是)
     */
    @Excel(name = "是否默认", readConverterExp = "0=否,1=是")
    private Integer isDefault;

    /**
     * 显示排序
     */
    @Excel(name = "显示排序")
    private Integer sortOrder;

    /**
     * 额外配置(JSON格式)
     */
    private String configExtra;

    /**
     * 状态(0-停用，1-启用)
     */
    @Excel(name = "状态", readConverterExp = "0=停用,1=启用")
    private Integer status;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 备注
     */
    @Excel(name = "备注")
    private String remark;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }


    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getModelType() {
        return modelType;
    }


    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }


    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public BigDecimal getTopP() {
        return topP;
    }

    public void setTopP(BigDecimal topP) {
        this.topP = topP;
    }

    public void setFrequencyPenalty(BigDecimal frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public BigDecimal getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setPresencePenalty(BigDecimal presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public BigDecimal getPresencePenalty() {
        return presencePenalty;
    }

    public void setStopSequences(String stopSequences) {
        this.stopSequences = stopSequences;
    }

    public String getStopSequences() {
        return stopSequences;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setConfigExtra(String configExtra) {
        this.configExtra = configExtra;
    }

    public String getConfigExtra() {
        return configExtra;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("provider", getProvider())
                .append("modelName", getModelName())
                .append("apiBaseUrl", getApiBaseUrl())
                .append("apiKey", getApiKey())
                .append("modelType", getModelType())
                .append("temperature", getTemperature())
                .append("maxTokens", getMaxTokens())
                .append("topP", getTopP())
                .append("topK", getTopK())
                .append("frequencyPenalty", getFrequencyPenalty())
                .append("presencePenalty", getPresencePenalty())
                .append("stopSequences", getStopSequences())
                .append("isDefault", getIsDefault())
                .append("sortOrder", getSortOrder())
                .append("configExtra", getConfigExtra())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }

}

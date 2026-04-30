package com.axzl.biz.knowledge.domain;

import com.axzl.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 知识库元数据表 biz_knowledge_meta
 *
 * @author liuchao
 */
@TableName("biz_knowledge_meta")
public class BizKnowledgeMeta extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 知识ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 问题/文本块内容 */
    private String question;

    /** 答案/补充内容 */
    private String answer;

    /** 分类标签 */
    private String label;

    /** 评分/重要度 */
    private Integer score;

    /** 关联疾病 */
    private String relatedDiseases;

    /** Milvus向量ID */
    private String vectorId;

    /** 向量状态(0未导入 1已导入 2失败) */
    private String vectorStatus;

    /** 来源类型(qa/doc) */
    private String sourceType;

    /** 来源文档ID */
    private Long sourceId;

    /** 分块序号 */
    private Integer chunkIndex;

    /** 数据来源 */
    private String source;

    /** 状态(0正常 1停用) */
    private String status;

    /** 删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getRelatedDiseases() {
        return relatedDiseases;
    }

    public void setRelatedDiseases(String relatedDiseases) {
        this.relatedDiseases = relatedDiseases;
    }

    public String getVectorId() {
        return vectorId;
    }

    public void setVectorId(String vectorId) {
        this.vectorId = vectorId;
    }

    public String getVectorStatus() {
        return vectorStatus;
    }

    public void setVectorStatus(String vectorStatus) {
        this.vectorStatus = vectorStatus;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("question", getQuestion())
                .append("answer", getAnswer())
                .append("label", getLabel())
                .append("score", getScore())
                .append("vectorId", getVectorId())
                .append("vectorStatus", getVectorStatus())
                .append("sourceType", getSourceType())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

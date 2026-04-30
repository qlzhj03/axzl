package com.axzl.biz.knowledge.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * 上传文档表 biz_document
 *
 * @author liuchao
 */
@TableName("biz_document")
public class BizDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文档ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 文件类型(txt/word/pdf/md) */
    private String fileType;

    /** 文件大小(字节) */
    private Long fileSize;

    /** Milvus对象存储路径 */
    private String milvusFilePath;

    /** 提取的文本内容 */
    private String extractedContent;

    /** 切分块数 */
    private Integer chunkCount;

    /** 向量状态(0未处理 1处理中 2已完成 3失败) */
    private String vectorStatus;

    /** 上传用户ID */
    private Long uploadBy;

    /** 状态(0正常 1删除) */
    private String status;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

    /** 备注 */
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMilvusFilePath() {
        return milvusFilePath;
    }

    public void setMilvusFilePath(String milvusFilePath) {
        this.milvusFilePath = milvusFilePath;
    }

    public String getExtractedContent() {
        return extractedContent;
    }

    public void setExtractedContent(String extractedContent) {
        this.extractedContent = extractedContent;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public String getVectorStatus() {
        return vectorStatus;
    }

    public void setVectorStatus(String vectorStatus) {
        this.vectorStatus = vectorStatus;
    }

    public Long getUploadBy() {
        return uploadBy;
    }

    public void setUploadBy(Long uploadBy) {
        this.uploadBy = uploadBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("fileName", getFileName())
                .append("fileType", getFileType())
                .append("fileSize", getFileSize())
                .append("vectorStatus", getVectorStatus())
                .append("createTime", getCreateTime())
                .toString();
    }
}

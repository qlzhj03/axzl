package com.axzl.biz.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 用药打卡记录表 biz_medication_log
 *
 * @author liuchao
 */
@TableName("biz_medication_log")
public class MedicationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用药ID */
    private Long medicationId;

    /** 用户ID */
    private Long userId;

    /** 计划时间 */
    private String scheduledTime;

    /** 实际服药时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date takenTime;

    /** 状态（0已打卡 1漏服） */
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicationId() { return medicationId; }
    public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    public Date getTakenTime() { return takenTime; }
    public void setTakenTime(Date takenTime) { this.takenTime = takenTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}

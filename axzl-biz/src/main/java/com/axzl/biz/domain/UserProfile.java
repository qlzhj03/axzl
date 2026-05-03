package com.axzl.biz.domain;

import com.axzl.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户健康档案表 biz_user_profile
 *
 * @author liuchao
 */
@TableName("biz_user_profile")
public class UserProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 档案ID */
    @TableId(type = IdType.AUTO)
    @Excel(name = "档案ID", cellType = Excel.ColumnType.NUMERIC, type = Excel.Type.EXPORT)
    private Long profileId;

    /** 用户ID */
    @Excel(name = "用户ID", cellType = Excel.ColumnType.NUMERIC, type = Excel.Type.EXPORT)
    private Long userId;

    /** 用户昵称（非数据库字段，用于展示） */
    @TableField(exist = false)
    @Excel(name = "用户昵称", type = Excel.Type.EXPORT)
    private String nickName;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出生日期", width = 30, dateFormat = "yyyy-MM-dd", type = Excel.Type.ALL)
    private Date birthday;

    /** 性别(0男 1女 2未知) */
    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知", type = Excel.Type.ALL)
    private String gender;

    /** 血型 */
    @Excel(name = "血型", type = Excel.Type.ALL)
    private String bloodType;

    /** 身高(cm) */
    @Excel(name = "身高(cm)", type = Excel.Type.ALL)
    private BigDecimal height;

    /** 体重(kg) */
    @Excel(name = "体重(kg)", type = Excel.Type.ALL)
    private BigDecimal weight;

    /** 过敏史 */
    @Excel(name = "过敏史", type = Excel.Type.ALL)
    private String allergyHistory;

    /** 既往病史 */
    @Excel(name = "既往病史", type = Excel.Type.ALL)
    private String medicalHistory;

    /** 家族病史 */
    @Excel(name = "家族病史", type = Excel.Type.ALL)
    private String familyHistory;

    /** 生活习惯 */
    @Excel(name = "生活习惯", type = Excel.Type.ALL)
    private String lifestyle;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用", type = Excel.Type.ALL)
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date updateTime;

    /** 删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

    /** 备注 */
    @Excel(name = "备注", type = Excel.Type.ALL)
    private String remark;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getAllergyHistory() {
        return allergyHistory;
    }

    public void setAllergyHistory(String allergyHistory) {
        this.allergyHistory = allergyHistory;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getFamilyHistory() {
        return familyHistory;
    }

    public void setFamilyHistory(String familyHistory) {
        this.familyHistory = familyHistory;
    }

    public String getLifestyle() {
        return lifestyle;
    }

    public void setLifestyle(String lifestyle) {
        this.lifestyle = lifestyle;
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
}

package com.axzl.biz.service;

import com.axzl.biz.domain.MedicationLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用药打卡记录 服务层
 *
 * @author liuchao
 */
public interface IMedicationLogService extends IService<MedicationLog> {

    /**
     * 打卡
     */
    int checkIn(Long medicationId, Long userId, String scheduledTime);

    /**
     * 查询用户某日的打卡记录
     */
    List<MedicationLog> selectLogsByDate(Long userId, String date);
}

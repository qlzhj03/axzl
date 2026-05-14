package com.axzl.biz.service;

import com.axzl.biz.domain.Medication;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用药提醒 服务层
 *
 * @author liuchao
 */
public interface IMedicationService extends IService<Medication> {

    /**
     * 查询用药列表
     */
    List<Medication> selectMedicationList(Medication medication);

    /**
     * 查询用户今日待提醒的用药
     */
    List<Medication> selectTodayReminders(Long userId);
}

package com.axzl.biz.service.impl;

import com.axzl.biz.domain.MedicationLog;
import com.axzl.biz.mapper.MedicationLogMapper;
import com.axzl.biz.service.IMedicationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 用药打卡记录 服务层实现
 *
 * @author liuchao
 */
@Service
public class MedicationLogServiceImpl extends ServiceImpl<MedicationLogMapper, MedicationLog>
        implements IMedicationLogService {

    @Override
    public int checkIn(Long medicationId, Long userId, String scheduledTime) {
        MedicationLog log = new MedicationLog();
        log.setMedicationId(medicationId);
        log.setUserId(userId);
        log.setScheduledTime(scheduledTime);
        log.setTakenTime(new Date());
        log.setStatus("0");
        return save(log) ? 1 : 0;
    }

    @Override
    public List<MedicationLog> selectLogsByDate(Long userId, String date) {
        LambdaQueryWrapper<MedicationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationLog::getUserId, userId);
        if (date != null && !date.isEmpty()) {
            wrapper.apply("date_format(create_time, '%Y-%m-%d') = {0}", date);
        }
        wrapper.orderByDesc(MedicationLog::getCreateTime);
        return list(wrapper);
    }
}

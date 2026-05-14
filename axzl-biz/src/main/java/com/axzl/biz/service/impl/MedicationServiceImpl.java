package com.axzl.biz.service.impl;

import com.axzl.biz.domain.Medication;
import com.axzl.biz.mapper.MedicationMapper;
import com.axzl.biz.service.IMedicationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 用药提醒 服务层实现
 *
 * @author liuchao
 */
@Service
public class MedicationServiceImpl extends ServiceImpl<MedicationMapper, Medication>
        implements IMedicationService {

    @Override
    public List<Medication> selectMedicationList(Medication medication) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        if (medication.getUserId() != null) {
            wrapper.eq(Medication::getUserId, medication.getUserId());
        }
        if (medication.getDrugName() != null && !medication.getDrugName().isEmpty()) {
            wrapper.like(Medication::getDrugName, medication.getDrugName());
        }
        if (medication.getStatus() != null && !medication.getStatus().isEmpty()) {
            wrapper.eq(Medication::getStatus, medication.getStatus());
        }
        wrapper.orderByDesc(Medication::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Medication> selectTodayReminders(Long userId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getUserId, userId)
               .eq(Medication::getStatus, "0")
               .and(w -> w.isNull(Medication::getEndDate)
                         .or()
                         .ge(Medication::getEndDate, today));
        wrapper.orderByAsc(Medication::getCreateTime);
        return list(wrapper);
    }
}

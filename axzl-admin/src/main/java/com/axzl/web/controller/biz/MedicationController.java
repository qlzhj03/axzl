package com.axzl.web.controller.biz;

import com.axzl.biz.domain.Medication;
import com.axzl.biz.domain.MedicationLog;
import com.axzl.biz.service.IMedicationLogService;
import com.axzl.biz.service.IMedicationService;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 用药提醒Controller
 *
 * @author liuchao
 */
@Tag(name = "用药提醒", description = "用药提醒管理相关接口")
@RestController
@RequestMapping("/biz/medication")
public class MedicationController extends BaseController {

    private final IMedicationService medicationService;
    private final IMedicationLogService medicationLogService;

    public MedicationController(IMedicationService medicationService,
                                IMedicationLogService medicationLogService) {
        this.medicationService = medicationService;
        this.medicationLogService = medicationLogService;
    }

    // ========== 管理端接口 ==========

    /**
     * 查询用药列表（管理端）
     */
    @Operation(summary = "查询用药列表", description = "分页查询用药提醒列表")
    @PreAuthorize("@ss.hasPermi('biz:medication:list')")
    @GetMapping("/list")
    public TableDataInfo list(Medication medication) {
        startPage();
        List<Medication> list = medicationService.selectMedicationList(medication);
        return getDataTable(list);
    }

    /**
     * 删除用药（管理端）
     */
    @Operation(summary = "删除用药", description = "批量删除用药提醒")
    @PreAuthorize("@ss.hasPermi('biz:medication:remove')")
    @Log(title = "用药提醒", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(
            @Parameter(description = "用药ID数组", required = true) @PathVariable Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            if (medicationService.removeById(id)) {
                count++;
            }
        }
        return toAjax(count);
    }

    // ========== 患者端接口 ==========

    /**
     * 获取我的用药列表
     */
    @Operation(summary = "获取我的用药列表", description = "获取当前用户的用药提醒列表")
    @GetMapping("/my")
    public AjaxResult my(Medication medication) {
        medication.setUserId(getUserId());
        List<Medication> list = medicationService.selectMedicationList(medication);
        return success(list);
    }

    /**
     * 获取今日用药提醒
     */
    @Operation(summary = "今日用药提醒", description = "获取当前用户今日待提醒的用药")
    @GetMapping("/my/today")
    public AjaxResult today() {
        Long userId = getUserId();
        List<Medication> medications = medicationService.selectTodayReminders(userId);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        List<MedicationLog> logs = medicationLogService.selectLogsByDate(userId, today);
        return success(java.util.Map.of("medications", medications, "logs", logs));
    }

    /**
     * 新增用药
     */
    @Operation(summary = "新增用药", description = "添加新的用药提醒")
    @PostMapping("/my")
    public AjaxResult add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用药信息", required = true)
            @RequestBody Medication medication) {
        medication.setUserId(getUserId());
        medication.setStatus("0");
        if (medication.getCreateTime() == null) {
            medication.setCreateTime(new Date());
        }
        medication.setUpdateTime(new Date());
        return toAjax(medicationService.save(medication) ? 1 : 0);
    }

    /**
     * 修改用药
     */
    @Operation(summary = "修改用药", description = "修改用药提醒信息")
    @PutMapping("/my")
    public AjaxResult edit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用药信息", required = true)
            @RequestBody Medication medication) {
        medication.setUserId(getUserId());
        medication.setUpdateTime(new Date());
        return toAjax(medicationService.updateById(medication) ? 1 : 0);
    }

    /**
     * 删除用药
     */
    @Operation(summary = "删除用药", description = "删除用药提醒")
    @DeleteMapping("/my/{ids}")
    public AjaxResult deleteMy(
            @Parameter(description = "用药ID数组", required = true) @PathVariable Long[] ids) {
        Long userId = getUserId();
        int count = 0;
        for (Long id : ids) {
            Medication m = medicationService.getById(id);
            if (m != null && userId.equals(m.getUserId())) {
                if (medicationService.removeById(id)) {
                    count++;
                }
            }
        }
        return toAjax(count);
    }

    /**
     * 用药打卡
     */
    @Operation(summary = "用药打卡", description = "记录用药打卡")
    @PostMapping("/my/checkin")
    public AjaxResult checkIn(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "打卡信息", required = true)
            @RequestBody java.util.Map<String, Object> params) {
        Long medicationId = Long.valueOf(params.get("medicationId").toString());
        String scheduledTime = params.get("scheduledTime") != null ? params.get("scheduledTime").toString() : null;
        Long userId = getUserId();
        return toAjax(medicationLogService.checkIn(medicationId, userId, scheduledTime));
    }

    /**
     * 获取今日打卡记录
     */
    @Operation(summary = "今日打卡记录", description = "获取当前用户今日的打卡记录")
    @GetMapping("/my/logs")
    public AjaxResult myLogs() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        List<MedicationLog> logs = medicationLogService.selectLogsByDate(getUserId(), today);
        return success(logs);
    }
}

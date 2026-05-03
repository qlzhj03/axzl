package com.axzl.web.controller.biz;

import com.axzl.biz.domain.UserProfile;
import com.axzl.biz.service.IUserProfileService;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.domain.entity.SysUser;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import com.axzl.common.utils.poi.ExcelUtil;
import com.axzl.system.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康档案Controller
 *
 * @author liuchao
 */
@Tag(name = "健康档案管理", description = "用户健康档案相关接口")
@RestController
@RequestMapping("/biz/profile")
public class ProfileController extends BaseController {

    private final IUserProfileService profileService;

    private final SysUserMapper sysUserMapper;

    public ProfileController(IUserProfileService profileService, SysUserMapper sysUserMapper) {
        this.profileService = profileService;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 查询健康档案列表
     */
    @Operation(summary = "查询健康档案列表", description = "分页查询健康档案列表")
    @PreAuthorize("@ss.hasPermi('biz:profile:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserProfile profile) {
        startPage();
        List<UserProfile> list = profileService.selectProfileList(profile);
        // 填充用户昵称
        fillUserNickNames(list);
        return getDataTable(list);
    }

    /**
     * 获取健康档案详细信息
     */
    @Operation(summary = "获取健康档案详情", description = "根据ID查询健康档案详细信息")
    @PreAuthorize("@ss.hasPermi('biz:profile:query')")
    @GetMapping("/{profileId}")
    public AjaxResult getInfo(
            @Parameter(description = "健康档案ID", required = true) @PathVariable Long profileId) {
        UserProfile profile = profileService.selectProfileById(profileId);
        // 填充用户昵称
        if (profile != null) {
            fillUserNickName(profile);
        }
        return success(profile);
    }

    /**
     * 新增健康档案
     */
    @Operation(summary = "新增健康档案", description = "创建新的健康档案")
    @PreAuthorize("@ss.hasPermi('biz:profile:add')")
    @Log(title = "健康档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "健康档案信息", required = true) 
                          @RequestBody UserProfile profile) {
        return toAjax(profileService.insertProfile(profile));
    }

    /**
     * 修改健康档案
     */
    @Operation(summary = "修改健康档案", description = "更新健康档案信息")
    @PreAuthorize("@ss.hasPermi('biz:profile:edit')")
    @Log(title = "健康档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "健康档案信息", required = true) 
                           @RequestBody UserProfile profile) {
        return toAjax(profileService.updateProfile(profile));
    }

    /**
     * 删除健康档案
     */
    @Operation(summary = "删除健康档案", description = "批量删除健康档案")
    @PreAuthorize("@ss.hasPermi('biz:profile:remove')")
    @Log(title = "健康档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{profileIds}")
    public AjaxResult remove(
            @Parameter(description = "健康档案ID数组", required = true) @PathVariable Long[] profileIds) {
        return toAjax(profileService.deleteProfileByIds(profileIds));
    }

    /**
     * 导出健康档案列表
     */
    @Operation(summary = "导出健康档案", description = "导出健康档案数据为Excel文件")
    @PreAuthorize("@ss.hasPermi('biz:profile:export')")
    @Log(title = "健康档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserProfile profile) {
        List<UserProfile> list = profileService.selectProfileList(profile);
        // 填充用户昵称
        fillUserNickNames(list);
        ExcelUtil<UserProfile> util = new ExcelUtil<>(UserProfile.class);
        util.exportExcel(response, list, "健康档案数据");
    }

    /**
     * 获取健康档案导入模板
     */
    @Operation(summary = "获取导入模板", description = "下载健康档案Excel导入模板")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<UserProfile> util = new ExcelUtil<>(UserProfile.class);
        util.importTemplateExcel(response, "健康档案数据");
    }

    /**
     * 导入健康档案数据
     */
    @Operation(summary = "导入健康档案", description = "从Excel文件导入健康档案数据")
    @PreAuthorize("@ss.hasPermi('biz:profile:import')")
    @Log(title = "健康档案", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(
            @Parameter(description = "Excel文件", required = true) MultipartFile file,
            @Parameter(description = "是否更新已存在数据") boolean updateSupport) throws Exception {
        ExcelUtil<UserProfile> util = new ExcelUtil<>(UserProfile.class);
        List<UserProfile> profileList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = profileService.importProfile(profileList, updateSupport, operName);
        return success(message);
    }

    /**
     * 获取当前用户健康档案（无需特殊权限，登录即可访问）
     */
    @Operation(summary = "获取我的健康档案", description = "获取当前登录用户的健康档案，不存在则自动创建")
    @GetMapping("/my")
    public AjaxResult getMyProfile() {
        Long userId = getUserId();
        UserProfile profile = profileService.selectProfileByUserId(userId);
        if (profile == null) {
            // 创建默认档案
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setGender("2");
            profile.setStatus("0");
            profile.setCreateTime(new Date());
            profileService.insertProfile(profile);
        }
        // 填充用户昵称
        fillUserNickName(profile);
        return success(profile);
    }

    /**
     * 更新当前用户健康档案（无需特殊权限，登录即可访问）
     */
    @Operation(summary = "更新我的健康档案", description = "更新当前登录用户的健康档案信息")
    @PostMapping("/my")
    public AjaxResult updateMyProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "健康档案信息", required = true) 
            @RequestBody UserProfile profile) {
        Long userId = getUserId();
        UserProfile existing = profileService.selectProfileByUserId(userId);
        if (existing == null) {
            profile.setUserId(userId);
            profile.setCreateTime(new Date());
            return toAjax(profileService.insertProfile(profile));
        }
        profile.setProfileId(existing.getProfileId());
        profile.setUserId(userId);
        return toAjax(profileService.updateProfile(profile));
    }

    /**
     * 批量填充用户昵称
     *
     * @param profiles 健康档案列表
     */
    private void fillUserNickNames(List<UserProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return;
        }
        
        // 收集所有需要查询的用户ID
        List<Long> userIds = profiles.stream()
                .map(UserProfile::getUserId)
                .filter(userId -> userId != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return;
        }
        
        // 批量查询用户信息（一次SQL查询）
        List<SysUser> users = sysUserMapper.selectUserByIds(userIds);
        Map<Long, String> userNickNameMap = users.stream()
                .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName, (v1, v2) -> v1));
        
        // 填充昵称
        profiles.forEach(profile -> {
            if (profile.getUserId() != null) {
                profile.setNickName(userNickNameMap.getOrDefault(profile.getUserId(), "未知用户"));
            }
        });
    }

    /**
     * 填充单个用户的昵称
     *
     * @param profile 健康档案对象
     */
    private void fillUserNickName(UserProfile profile) {
        if (profile == null || profile.getUserId() == null) {
            return;
        }
        
        SysUser user = sysUserMapper.selectUserById(profile.getUserId());
        if (user != null) {
            profile.setNickName(user.getNickName());
        } else {
            profile.setNickName("未知用户");
        }
    }
}

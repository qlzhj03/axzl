package com.axzl.web.controller.biz.profile;

import com.axzl.biz.profile.domain.UserProfile;
import com.axzl.biz.profile.service.IUserProfileService;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.core.domain.entity.SysUser;
import com.axzl.common.core.page.TableDataInfo;
import com.axzl.common.enums.BusinessType;
import com.axzl.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康档案Controller
 *
 * @author liuchao
 */
@RestController
@RequestMapping("/biz/profile")
public class ProfileController extends BaseController {

    private final IUserProfileService profileService;

    @Autowired
    private SysUserMapper sysUserMapper;

    public ProfileController(IUserProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 查询健康档案列表
     */
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
    @PreAuthorize("@ss.hasPermi('biz:profile:query')")
    @GetMapping("/{profileId}")
    public AjaxResult getInfo(@PathVariable Long profileId) {
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
    @PreAuthorize("@ss.hasPermi('biz:profile:add')")
    @Log(title = "健康档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserProfile profile) {
        return toAjax(profileService.insertProfile(profile));
    }

    /**
     * 修改健康档案
     */
    @PreAuthorize("@ss.hasPermi('biz:profile:edit')")
    @Log(title = "健康档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserProfile profile) {
        return toAjax(profileService.updateProfile(profile));
    }

    /**
     * 删除健康档案
     */
    @PreAuthorize("@ss.hasPermi('biz:profile:remove')")
    @Log(title = "健康档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{profileIds}")
    public AjaxResult remove(@PathVariable Long[] profileIds) {
        return toAjax(profileService.deleteProfileByIds(profileIds));
    }

    /**
     * 获取当前用户健康档案（无需特殊权限，登录即可访问）
     */
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
    @PostMapping("/my")
    public AjaxResult updateMyProfile(@RequestBody UserProfile profile) {
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

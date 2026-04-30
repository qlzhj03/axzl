package com.axzl.biz.profile.service.impl;

import com.axzl.biz.profile.domain.UserProfile;
import com.axzl.biz.profile.mapper.UserProfileMapper;
import com.axzl.biz.profile.service.IUserProfileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 用户健康档案 服务层实现类
 * <p>
 * 提供用户健康档案的增删改查等核心业务功能，继承MyBatis-Plus的ServiceImpl基类，
 * 实现了IUserProfileService接口定义的所有方法。
 * </p>
 *
 * @author liuchao
 */
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile>
        implements IUserProfileService {

    /**
     * 根据条件查询用户健康档案列表
     * <p>
     * 支持按用户ID、性别和状态进行筛选，结果按创建时间降序排列。
     * </p>
     *
     * @param profile 查询条件对象，包含可选的用户ID、性别和状态过滤条件
     * @return 符合条件的用户健康档案列表
     */
    @Override
    public List<UserProfile> selectProfileList(UserProfile profile) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        if (profile.getUserId() != null) {
            wrapper.eq(UserProfile::getUserId, profile.getUserId());
        }
        if (profile.getGender() != null && !profile.getGender().isEmpty()) {
            wrapper.eq(UserProfile::getGender, profile.getGender());
        }
        if (profile.getStatus() != null && !profile.getStatus().isEmpty()) {
            wrapper.eq(UserProfile::getStatus, profile.getStatus());
        }
        wrapper.orderByDesc(UserProfile::getCreateTime).orderByDesc(UserProfile::getProfileId);
        return list(wrapper);
    }

    /**
     * 根据档案ID查询用户健康档案详情
     *
     * @param profileId 档案ID
     * @return 对应的用户健康档案对象，如果不存在则返回null
     */
    @Override
    public UserProfile selectProfileById(Long profileId) {
        return getById(profileId);
    }

    /**
     * 根据用户ID查询其健康档案信息
     * <p>
     * 通常一个用户只对应一份健康档案，因此使用getOne方法获取单个结果。
     * </p>
     *
     * @param userId 用户ID
     * @return 对应用户的健康档案对象，如果不存在则返回null
     */
    @Override
    public UserProfile selectProfileByUserId(Long userId) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        return getOne(wrapper);
    }

    /**
     * 新增用户健康档案记录
     * <p>
     * 自动设置创建时间和更新时间，如果传入的对象中已包含创建时间则保持不变。
     * </p>
     *
     * @param profile 待保存的用户健康档案对象
     * @return 成功返回1，失败返回0
     */
    @Override
    public int insertProfile(UserProfile profile) {
        if (profile.getCreateTime() == null) {
            profile.setCreateTime(new Date());
        }
        profile.setUpdateTime(new Date());
        return save(profile) ? 1 : 0;
    }

    /**
     * 更新用户健康档案信息
     * <p>
     * 自动更新修改时间为当前时间，确保数据变更可追溯。
     * </p>
     *
     * @param profile 待更新的用户健康档案对象，必须包含有效的档案ID
     * @return 成功返回1，失败返回0
     */
    @Override
    public int updateProfile(UserProfile profile) {
        profile.setUpdateTime(new Date());
        return updateById(profile) ? 1 : 0;
    }

    /**
     * 批量删除用户健康档案记录
     * <p>
     * 逐个删除指定的档案记录，并统计实际成功删除的数量。
     * </p>
     *
     * @param profileIds 待删除的档案ID数组
     * @return 实际成功删除的记录数量
     */
    @Override
    public int deleteProfileByIds(Long[] profileIds) {
        int count = 0;
        for (Long id : profileIds) {
            if (removeById(id)) {
                count++;
            }
        }
        return count;
    }
}

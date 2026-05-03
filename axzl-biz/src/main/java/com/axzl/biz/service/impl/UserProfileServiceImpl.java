package com.axzl.biz.service.impl;

import com.axzl.biz.domain.UserProfile;
import com.axzl.biz.mapper.UserProfileMapper;
import com.axzl.biz.service.IUserProfileService;
import com.axzl.common.utils.StringUtils;
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

    /**
     * 导入健康档案数据
     * <p>
     * 根据updateSupport参数决定是新增还是更新已存在的档案数据。
     * </p>
     *
     * @param profileList 档案数据列表
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果信息
     */
    @Override
    public String importProfile(List<UserProfile> profileList, boolean updateSupport, String operName) {
        if (StringUtils.isNull(profileList) || profileList.size() == 0) {
            throw new RuntimeException("导入健康档案数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        
        for (UserProfile profile : profileList) {
            try {
                // 验证是否存在这个用户的档案
                UserProfile existProfile = selectProfileByUserId(profile.getUserId());
                
                if (StringUtils.isNull(existProfile)) {
                    // 新增档案
                    profile.setCreateTime(new Date());
                    profile.setUpdateTime(new Date());
                    profile.setStatus(StringUtils.isEmpty(profile.getStatus()) ? "0" : profile.getStatus());
                    this.save(profile);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、用户ID ").append(profile.getUserId()).append(" 的档案导入成功");
                } else if (updateSupport) {
                    // 更新档案
                    profile.setProfileId(existProfile.getProfileId());
                    profile.setUpdateTime(new Date());
                    this.updateById(profile);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、用户ID ").append(profile.getUserId()).append(" 的档案更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、用户ID ").append(profile.getUserId()).append(" 的档案已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、用户ID " + profile.getUserId() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
            }
        }
        
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new RuntimeException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}

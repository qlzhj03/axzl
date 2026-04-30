package com.axzl.biz.profile.service;

import com.axzl.biz.profile.domain.UserProfile;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户健康档案 服务层
 *
 * @author liuchao
 */
public interface IUserProfileService extends IService<UserProfile> {

    /**
     * 查询档案列表
     *
     * @param profile 查询条件
     * @return 档案集合
     */
    List<UserProfile> selectProfileList(UserProfile profile);

    /**
     * 通过ID查询档案
     *
     * @param profileId 档案ID
     * @return 档案信息
     */
    UserProfile selectProfileById(Long profileId);

    /**
     * 通过用户ID查询档案
     *
     * @param userId 用户ID
     * @return 档案信息
     */
    UserProfile selectProfileByUserId(Long userId);

    /**
     * 新增档案
     *
     * @param profile 档案信息
     * @return 结果
     */
    int insertProfile(UserProfile profile);

    /**
     * 修改档案
     *
     * @param profile 档案信息
     * @return 结果
     */
    int updateProfile(UserProfile profile);

    /**
     * 批量删除档案
     *
     * @param profileIds 需要删除的档案ID
     * @return 结果
     */
    int deleteProfileByIds(Long[] profileIds);
}

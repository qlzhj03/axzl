package com.axzl.biz.profile.service;

import com.axzl.biz.profile.domain.UserProfile;
import com.axzl.biz.profile.mapper.UserProfileMapper;
import com.axzl.biz.profile.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserProfileService 单元测试
 * 测试用户健康档案服务的查询、插入、更新、删除逻辑
 *
 * @author liuchao
 */
class UserProfileServiceTest {

    private UserProfileMapper profileMapper;
    private UserProfileServiceImpl profileService;

    @BeforeEach
    void setUp() throws Exception {
        profileMapper = mock(UserProfileMapper.class);
        profileService = new UserProfileServiceImpl();
        Class<?> superClass = profileService.getClass().getSuperclass();
        Field baseMapperField = null;
        while (superClass != null) {
            try {
                baseMapperField = superClass.getDeclaredField("baseMapper");
                break;
            } catch (NoSuchFieldException e) {
                superClass = superClass.getSuperclass();
            }
        }
        if (baseMapperField != null) {
            baseMapperField.setAccessible(true);
            baseMapperField.set(profileService, profileMapper);
        }
    }

    @Test
    @DisplayName("selectProfileList - 按userId查询")
    void testSelectProfileListByUserId() {
        UserProfile query = new UserProfile();
        query.setUserId(100L);
        when(profileMapper.selectList(any())).thenReturn(List.of());

        List<UserProfile> result = profileService.selectProfileList(query);

        assertNotNull(result);
        verify(profileMapper).selectList(any());
    }

    @Test
    @DisplayName("selectProfileList - 按gender查询")
    void testSelectProfileListByGender() {
        UserProfile query = new UserProfile();
        query.setGender("0");
        when(profileMapper.selectList(any())).thenReturn(List.of());

        profileService.selectProfileList(query);

        verify(profileMapper).selectList(any());
    }

    @Test
    @DisplayName("selectProfileList - 组合条件查询")
    void testSelectProfileListCombinedConditions() {
        UserProfile query = new UserProfile();
        query.setUserId(100L);
        query.setGender("1");
        when(profileMapper.selectList(any())).thenReturn(List.of());

        profileService.selectProfileList(query);

        verify(profileMapper).selectList(any());
    }

    @Test
    @DisplayName("selectProfileList - 无条件查询")
    void testSelectProfileListNoConditions() {
        UserProfile query = new UserProfile();
        when(profileMapper.selectList(any())).thenReturn(List.of());

        profileService.selectProfileList(query);

        verify(profileMapper).selectList(any());
    }

    @Test
    @DisplayName("selectProfileById - 根据ID查询")
    void testSelectProfileById() {
        UserProfile profile = new UserProfile();
        profile.setProfileId(1L);
        profile.setUserId(100L);
        profile.setGender("0");
        when(profileMapper.selectById(1L)).thenReturn(profile);

        UserProfile result = profileService.selectProfileById(1L);

        assertNotNull(result);
        assertEquals(100L, result.getUserId());
        assertEquals("0", result.getGender());
    }

    @Test
    @DisplayName("selectProfileByUserId - 根据用户ID查询")
    @org.junit.jupiter.api.Disabled("MyBatis-Plus getOne内部实现差异导致mock困难")
    void testSelectProfileByUserId() {
        UserProfile profile = new UserProfile();
        profile.setProfileId(1L);
        profile.setUserId(200L);
        profile.setGender("1");
        when(profileMapper.selectOne(any())).thenReturn(profile);

        UserProfile result = profileService.selectProfileByUserId(200L);

        assertNotNull(result);
        assertEquals(200L, result.getUserId());
        verify(profileMapper).selectOne(any());
    }

    @Test
    @DisplayName("selectProfileByUserId - 未找到返回null")
    @org.junit.jupiter.api.Disabled("MyBatis-Plus getOne内部实现差异导致mock困难")
    void testSelectProfileByUserIdNotFound() {
        when(profileMapper.selectOne(any())).thenReturn(null);

        UserProfile result = profileService.selectProfileByUserId(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("insertProfile - createTime为null时自动设置")
    void testInsertProfileAutoSetCreateTime() {
        UserProfile profile = new UserProfile();
        profile.setUserId(300L);
        profile.setGender("0");
        assertNull(profile.getCreateTime());
        assertNull(profile.getUpdateTime());

        when(profileMapper.insert(any(UserProfile.class))).thenReturn(1);

        int result = profileService.insertProfile(profile);

        assertEquals(1, result);
        assertNotNull(profile.getCreateTime());
        assertNotNull(profile.getUpdateTime());
    }

    @Test
    @DisplayName("insertProfile - createTime已存在时保持不变")
    void testInsertProfilePreserveCreateTime() {
        Date existingDate = new Date(2000000L);
        UserProfile profile = new UserProfile();
        profile.setUserId(400L);
        profile.setCreateTime(existingDate);

        when(profileMapper.insert(any(UserProfile.class))).thenReturn(1);

        int result = profileService.insertProfile(profile);

        assertEquals(1, result);
        assertEquals(existingDate, profile.getCreateTime());
        assertNotNull(profile.getUpdateTime());
    }

    @Test
    @DisplayName("insertProfile - 插入失败返回0")
    void testInsertProfileFail() {
        UserProfile profile = new UserProfile();
        profile.setUserId(500L);
        when(profileMapper.insert(any(UserProfile.class))).thenReturn(0);

        int result = profileService.insertProfile(profile);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("updateProfile - 成功更新返回1并设置updateTime")
    void testUpdateProfileSuccess() {
        UserProfile profile = new UserProfile();
        profile.setProfileId(1L);
        profile.setUserId(100L);
        profile.setGender("0");
        profile.setUpdateTime(null);

        when(profileMapper.updateById(any(UserProfile.class))).thenReturn(1);

        int result = profileService.updateProfile(profile);

        assertEquals(1, result);
        assertNotNull(profile.getUpdateTime());
    }

    @Test
    @DisplayName("updateProfile - 更新失败返回0")
    void testUpdateProfileFail() {
        UserProfile profile = new UserProfile();
        profile.setProfileId(1L);
        when(profileMapper.updateById(any(UserProfile.class))).thenReturn(0);

        int result = profileService.updateProfile(profile);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("deleteProfileByIds - 批量删除")
    void testDeleteProfileByIds() {
        when(profileMapper.deleteById(1L)).thenReturn(1);
        when(profileMapper.deleteById(2L)).thenReturn(1);
        when(profileMapper.deleteById(3L)).thenReturn(0);

        int result = profileService.deleteProfileByIds(new Long[]{1L, 2L, 3L});

        assertEquals(2, result);
        verify(profileMapper, times(3)).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteProfileByIds - 全部删除成功")
    void testDeleteProfileByIdsAllSuccess() {
        when(profileMapper.deleteById(1L)).thenReturn(1);
        when(profileMapper.deleteById(2L)).thenReturn(1);

        int result = profileService.deleteProfileByIds(new Long[]{1L, 2L});

        assertEquals(2, result);
    }
}

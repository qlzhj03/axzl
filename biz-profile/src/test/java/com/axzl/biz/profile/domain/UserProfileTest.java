package com.axzl.biz.profile.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户档案实体单元测试
 *
 * @author liuchao
 */
class UserProfileTest {

    @Test
    @DisplayName("UserProfile - 所有字段getter/setter正常")
    void testProfileFieldAccess() {
        UserProfile profile = new UserProfile();
        Date now = new Date();
        Date birthday = new Date();

        profile.setProfileId(1L);
        profile.setUserId(100L);
        profile.setBirthday(birthday);
        profile.setGender("0");
        profile.setBloodType("A");
        profile.setHeight(new BigDecimal("170.5"));
        profile.setWeight(new BigDecimal("65.0"));
        profile.setAllergyHistory("青霉素过敏");
        profile.setMedicalHistory("高血压");
        profile.setFamilyHistory("父亲有糖尿病");
        profile.setLifestyle("每天跑步30分钟");
        profile.setStatus("0");
        profile.setCreateTime(now);
        profile.setUpdateTime(now);
        profile.setRemark("备注");

        assertAll("UserProfile字段验证",
                () -> assertEquals(1L, profile.getProfileId()),
                () -> assertEquals(100L, profile.getUserId()),
                () -> assertEquals(birthday, profile.getBirthday()),
                () -> assertEquals("0", profile.getGender()),
                () -> assertEquals("A", profile.getBloodType()),
                () -> assertEquals(new BigDecimal("170.5"), profile.getHeight()),
                () -> assertEquals(new BigDecimal("65.0"), profile.getWeight()),
                () -> assertEquals("青霉素过敏", profile.getAllergyHistory()),
                () -> assertEquals("高血压", profile.getMedicalHistory()),
                () -> assertEquals("父亲有糖尿病", profile.getFamilyHistory()),
                () -> assertEquals("每天跑步30分钟", profile.getLifestyle()),
                () -> assertEquals("0", profile.getStatus()),
                () -> assertEquals(now, profile.getCreateTime()),
                () -> assertEquals("备注", profile.getRemark())
        );
    }

    @Test
    @DisplayName("UserProfile - 性别编码正确")
    void testGenderCodes() {
        UserProfile male = new UserProfile();
        male.setGender("0");
        assertEquals("0", male.getGender());

        UserProfile female = new UserProfile();
        female.setGender("1");
        assertEquals("1", female.getGender());

        UserProfile unknown = new UserProfile();
        unknown.setGender("2");
        assertEquals("2", unknown.getGender());
    }

    @Test
    @DisplayName("UserProfile - 身高体重数值精度")
    void testHeightWeightPrecision() {
        UserProfile profile = new UserProfile();
        profile.setHeight(new BigDecimal("170.50"));
        profile.setWeight(new BigDecimal("65.00"));

        assertEquals(0, new BigDecimal("170.50").compareTo(profile.getHeight()));
        assertEquals(0, new BigDecimal("65.00").compareTo(profile.getWeight()));
    }

    @Test
    @DisplayName("UserProfile - 过敏史为空时的处理")
    void testEmptyAllergyHistory() {
        UserProfile profile = new UserProfile();

        assertNull(profile.getAllergyHistory());
        assertNull(profile.getMedicalHistory());
        assertNull(profile.getFamilyHistory());
    }
}

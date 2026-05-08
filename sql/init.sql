-- ============================================================================
-- 安心诊聊平台（AXZL）数据库初始化脚本
-- 版本: v1.0
-- 更新日期: 2026-05-03
-- ============================================================================

-- 设置字符集以防止中文乱码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;
SET character_set_database=utf8mb4;
SET character_set_results=utf8mb4;
SET character_set_server=utf8mb4;

-- ============================================================================
-- 第一部分: 数据库创建
-- ============================================================================

DROP DATABASE IF EXISTS axzl;
CREATE DATABASE axzl
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE axzl;


-- ============================================================================
-- 第二部分: 系统基础表
-- 包含: 用户、角色、菜单、用户角色关联、角色菜单关联、
--       操作日志、字典类型/数据、参数配置、登录日志
-- ============================================================================

-- ----------------------------
-- 1、用户信息表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    user_id         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    user_name       VARCHAR(30)  NOT NULL                COMMENT '用户账号',
    nick_name       VARCHAR(30)  NOT NULL                COMMENT '用户昵称',
    user_type       VARCHAR(2)   DEFAULT '00'            COMMENT '用户类型（00系统用户）',
    email           VARCHAR(50)  DEFAULT ''              COMMENT '用户邮箱',
    phonenumber     VARCHAR(11)  DEFAULT ''              COMMENT '手机号码',
    sex             CHAR(1)      DEFAULT '0'             COMMENT '用户性别（0男 1女 2未知）',
    avatar          VARCHAR(100) DEFAULT ''              COMMENT '头像地址',
    password        VARCHAR(100) DEFAULT ''              COMMENT '密码',
    status          CHAR(1)      DEFAULT '0'             COMMENT '账号状态（0正常 1停用）',
    del_flag        CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
    login_ip        VARCHAR(128) DEFAULT ''              COMMENT '最后登录IP',
    login_date      DATETIME                             COMMENT '最后登录时间',
    pwd_update_date DATETIME                             COMMENT '密码最后更新时间',
    create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME                             COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME                             COMMENT '更新时间',
    remark          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='用户信息表';

-- 管理员账号 (默认密码: admin123)
INSERT INTO sys_user VALUES(1, 'admin', '安欣诊聊', '00', 'axzl@163.com', '15888888888', '1', '',
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0',
    '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '管理员');


-- ----------------------------
-- 2、角色信息表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    role_id             BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name           VARCHAR(30)  NOT NULL                COMMENT '角色名称',
    role_key            VARCHAR(100) NOT NULL                COMMENT '角色权限字符串',
    role_sort           INT(4)       NOT NULL                COMMENT '显示顺序',
    data_scope          CHAR(1)      DEFAULT '2'             COMMENT '数据范围（1：全部数据权限 2：自定数据权限）',
    menu_check_strictly TINYINT(1)   DEFAULT 1               COMMENT '菜单树选择项是否关联显示',
    status              CHAR(1)      NOT NULL                COMMENT '角色状态（0正常 1停用）',
    del_flag            CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
    create_by           VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time         DATETIME                             COMMENT '创建时间',
    update_by           VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time         DATETIME                             COMMENT '更新时间',
    remark              VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (role_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='角色信息表';

-- 初始化角色
INSERT INTO sys_role VALUES('1', '超级管理员', 'admin',          1, 1, 1, '0', '0', 'admin', SYSDATE(), '', NULL, '超级管理员');
INSERT INTO sys_role VALUES('2', '医疗专家',   'medical_expert', 2, 2, 1, '0', '0', 'admin', SYSDATE(), '', NULL, '医疗专家角色，具有知识库权限');
INSERT INTO sys_role VALUES('3', '普通医生',   'doctor',         3, 2, 1, '0', '0', 'admin', SYSDATE(), '', NULL, '普通医生角色，具有诊疗权限');
INSERT INTO sys_role VALUES('4', '护士助理',   'nurse_assistant',4, 2, 1, '0', '0', 'admin', SYSDATE(), '', NULL, '护士助理角色，协助医生工作');
INSERT INTO sys_role VALUES('5', '患者',       'patient',        5, 2, 1, '0', '0', 'admin', SYSDATE(), '', NULL, '患者角色，使用问诊服务');


-- ----------------------------
-- 3、菜单权限表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    menu_id     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    menu_name   VARCHAR(50)  NOT NULL                COMMENT '菜单名称',
    parent_id   BIGINT(20)   DEFAULT 0               COMMENT '父菜单ID',
    order_num   INT(4)       DEFAULT 0               COMMENT '显示顺序',
    path        VARCHAR(200) DEFAULT ''              COMMENT '路由地址',
    component   VARCHAR(255) DEFAULT NULL            COMMENT '组件路径',
    query       VARCHAR(255) DEFAULT NULL            COMMENT '路由参数',
    route_name  VARCHAR(50)  DEFAULT ''              COMMENT '路由名称',
    is_frame    INT(1)       DEFAULT 1               COMMENT '是否为外链（0是 1否）',
    is_cache    INT(1)       DEFAULT 0               COMMENT '是否缓存（0缓存 1不缓存）',
    menu_type   CHAR(1)      DEFAULT ''              COMMENT '菜单类型（M目录 C菜单 F按钮）',
    visible     CHAR(1)      DEFAULT 0               COMMENT '菜单状态（0显示 1隐藏）',
    status      CHAR(1)      DEFAULT 0               COMMENT '菜单状态（0正常 1停用）',
    perms       VARCHAR(100) DEFAULT NULL            COMMENT '权限标识',
    icon        VARCHAR(100) DEFAULT '#'             COMMENT '菜单图标',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME                             COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME                             COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT ''              COMMENT '备注',
    PRIMARY KEY (menu_id)
) ENGINE=InnoDB AUTO_INCREMENT=2000 COMMENT='菜单权限表';

-- ---- 一级目录 ----
INSERT INTO sys_menu VALUES('1', '系统管理', '0', '1', 'system',  NULL, '', '', 1, 0, 'M', '0', '0', '', 'system',  'admin', SYSDATE(), '', NULL, '系统管理目录');
INSERT INTO sys_menu VALUES('2', '系统监控', '0', '2', 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', SYSDATE(), '', NULL, '系统监控目录');
INSERT INTO sys_menu VALUES('3', '系统工具', '0', '3', 'tool',    NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool',    'admin', SYSDATE(), '', NULL, '系统工具目录');
INSERT INTO sys_menu VALUES('4', '业务管理', '0', '4', 'biz',     NULL, '', '', 1, 0, 'M', '0', '0', '', 'guide',   'admin', SYSDATE(), '', NULL, '业务管理目录');

-- ---- 系统管理 - 二级菜单 ----
INSERT INTO sys_menu VALUES('100', '用户管理', '1', '1', 'user',       'system/user/index',        '', '', 1, 0, 'C', '0', '0', 'system:user:list',   'user',       'admin', SYSDATE(), '', NULL, '用户管理菜单');
INSERT INTO sys_menu VALUES('101', '角色管理', '1', '2', 'role',       'system/role/index',        '', '', 1, 0, 'C', '0', '0', 'system:role:list',   'peoples',    'admin', SYSDATE(), '', NULL, '角色管理菜单');
INSERT INTO sys_menu VALUES('102', '菜单管理', '1', '3', 'menu',       'system/menu/index',        '', '', 1, 0, 'C', '0', '0', 'system:menu:list',   'tree-table', 'admin', SYSDATE(), '', NULL, '菜单管理菜单');
INSERT INTO sys_menu VALUES('105', '字典管理', '1', '6', 'dict',       'system/dict/index',        '', '', 1, 0, 'C', '0', '0', 'system:dict:list',   'dict',       'admin', SYSDATE(), '', NULL, '字典管理菜单');
INSERT INTO sys_menu VALUES('106', '参数设置', '1', '7', 'config',     'system/config/index',      '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit',       'admin', SYSDATE(), '', NULL, '参数设置菜单');
INSERT INTO sys_menu VALUES('108', '日志管理', '1', '8', 'log',        '',                         '', '', 1, 0, 'M', '0', '0', '',                   'log',        'admin', SYSDATE(), '', NULL, '日志管理菜单');

-- ---- 系统监控 - 二级菜单 ----
INSERT INTO sys_menu VALUES('109', '在线用户', '2', '1', 'online',     'monitor/online/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:online:list',  'online',     'admin', SYSDATE(), '', NULL, '在线用户菜单');
INSERT INTO sys_menu VALUES('111', '数据监控', '2', '3', 'druid',      'monitor/druid/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list',   'druid',      'admin', SYSDATE(), '', NULL, '数据监控菜单');
INSERT INTO sys_menu VALUES('112', '服务监控', '2', '4', 'server',     'monitor/server/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:server:list',  'server',     'admin', SYSDATE(), '', NULL, '服务监控菜单');
INSERT INTO sys_menu VALUES('113', '缓存监控', '2', '5', 'cache',      'monitor/cache/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',   'redis',      'admin', SYSDATE(), '', NULL, '缓存监控菜单');
INSERT INTO sys_menu VALUES('114', '缓存列表', '2', '6', 'cacheList',  'monitor/cache/list',       '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',   'redis-list', 'admin', SYSDATE(), '', NULL, '缓存列表菜单');

-- ---- 系统工具 - 二级菜单 ----
INSERT INTO sys_menu VALUES('117', '系统接口', '3', '1', 'swagger',    'tool/swagger/index',       '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list',  'swagger',    'admin', SYSDATE(), '', NULL, '系统接口菜单');
INSERT INTO sys_menu VALUES('118', 'AI模型管理', '3', '2', 'ai-model', 'system/aiModel/index',     '', '', 1, 0, 'C', '0', '0', 'system:aiModel:list', 'cpu',        'admin', SYSDATE(), '', NULL, 'AI大模型参数管理菜单');

-- ---- 日志管理 - 三级菜单 ----
INSERT INTO sys_menu VALUES('500', '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',       'admin', SYSDATE(), '', NULL, '操作日志菜单');
INSERT INTO sys_menu VALUES('501', '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', SYSDATE(), '', NULL, '登录日志菜单');

-- ---- 用户管理按钮 (1000-1006) ----
INSERT INTO sys_menu VALUES('1000', '用户查询', '100', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1001', '用户新增', '100', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',      '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1002', '用户修改', '100', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',     '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1003', '用户删除', '100', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1004', '用户导出', '100', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1005', '用户导入', '100', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1006', '重置密码', '100', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 角色管理按钮 (1007-1011) ----
INSERT INTO sys_menu VALUES('1007', '角色查询', '101', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1008', '角色新增', '101', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1009', '角色修改', '101', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1010', '角色删除', '101', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1011', '角色导出', '101', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 菜单管理按钮 (1012-1015) ----
INSERT INTO sys_menu VALUES('1012', '菜单查询', '102', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1013', '菜单新增', '102', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1014', '菜单修改', '102', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1015', '菜单删除', '102', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 字典管理按钮 (1025-1029) ----
INSERT INTO sys_menu VALUES('1025', '字典查询', '105', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1026', '字典新增', '105', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1027', '字典修改', '105', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1028', '字典删除', '105', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1029', '字典导出', '105', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 参数设置按钮 (1030-1034) ----
INSERT INTO sys_menu VALUES('1030', '参数查询', '106', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1031', '参数新增', '106', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1032', '参数修改', '106', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1033', '参数删除', '106', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1034', '参数导出', '106', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 操作日志按钮 (1039-1041) ----
INSERT INTO sys_menu VALUES('1039', '操作查询', '500', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1040', '操作删除', '500', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1041', '日志导出', '500', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 登录日志按钮 (1042-1045) ----
INSERT INTO sys_menu VALUES('1042', '登录查询', '501', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1043', '登录删除', '501', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1044', '日志导出', '501', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1045', '账户解锁', '501', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 在线用户按钮 (1046-1048) ----
INSERT INTO sys_menu VALUES('1046', '在线查询', '109', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1047', '批量强退', '109', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1048', '单条强退', '109', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- AI模型管理按钮 (1050-1055) ----
INSERT INTO sys_menu VALUES('1050', '模型查询', '118', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:aiModel:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1051', '模型新增', '118', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:aiModel:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1052', '模型修改', '118', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:aiModel:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1053', '模型删除', '118', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:aiModel:remove', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1054', '模型导出', '118', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:aiModel:export', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('1055', '模型切换', '118', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:aiModel:switch', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 业务管理 - 知识库管理 (401 + 4001-4004) ----
INSERT INTO sys_menu VALUES('401',  '知识库管理', '4', '1', 'knowledge', 'biz/knowledge/index', '', '', 1, 0, 'C', '0', '0', 'biz:knowledge:list',   'education', 'admin', SYSDATE(), '', NULL, '知识库管理菜单');
INSERT INTO sys_menu VALUES('4001', '知识查询',   '401', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:knowledge:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4002', '知识新增',   '401', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:knowledge:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4003', '知识修改',   '401', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:knowledge:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4004', '知识删除',   '401', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:knowledge:remove', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 业务管理 - 文档管理 (402 + 4005-4006, 4018-4019) ----
INSERT INTO sys_menu VALUES('402',  '文档管理', '4', '2', 'document', 'biz/document/index', '', '', 1, 0, 'C', '0', '0', 'biz:document:list',    'documentation', 'admin', SYSDATE(), '', NULL, '文档管理菜单');
INSERT INTO sys_menu VALUES('4005', '文档查询', '402', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:document:query',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4006', '文档处理', '402', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:document:process', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4018', '文档删除', '402', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:document:remove',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4019', '文档导出', '402', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:document:export',  '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 业务管理 - 问诊记录 (403 + 4007-4008, 4013-4017, 4020-4022) ----
INSERT INTO sys_menu VALUES('403',  '问诊记录',   '4', '3', 'consultation', 'biz/consultation/index', '', '', 1, 0, 'C', '0', '0', 'biz:consultation:list',       'message',        'admin', SYSDATE(), '', NULL, '问诊记录菜单');
INSERT INTO sys_menu VALUES('4007', '问诊查询',   '403', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:query',      '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4008', '问诊删除',   '403', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:remove',     '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4013', '流式对话',   '403', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:chat',        '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4014', '问诊历史',   '403', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:history',     '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4015', '问诊详情',   '403', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:detail',      '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4016', '满意度评价', '403', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:satisfaction','#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4017', '会话重命名', '403', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:rename',      '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4020', '问诊编辑',   '403', '8', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:edit',        '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4021', '问诊置顶',   '403', '9', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:pin',         '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4022', '满意度评分', '403', '10', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:satisfaction','#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 业务管理 - 健康档案管理 (404 + 4009-4012, 4015-4016) ----
-- 注意: 4015/4016 与问诊记录的菜单ID冲突，这里使用档案导出/档案导入
INSERT INTO sys_menu VALUES('404',  '健康档案管理', '4', '4', 'health-profile', 'biz/profile/index', '', 'HealthProfile', 1, 0, 'C', '0', '0', 'biz:profile:list',   'profile', 'admin', SYSDATE(), '', NULL, '健康档案管理菜单');
INSERT INTO sys_menu VALUES('4009', '档案新增',     '404', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4010', '档案修改',     '404', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4011', '档案查询',     '404', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4012', '档案删除',     '404', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:remove', '#', 'admin', SYSDATE(), '', NULL, '');
-- 注意: biz.sql中4015=档案导出, 4016=档案导入，与问诊4015/4016冲突
-- 这里保留问诊的4015(问诊详情)/4016(满意度评价)，档案导出导入使用不同ID
INSERT INTO sys_menu VALUES('4023', '档案导出',     '404', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:export', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4024', '档案导入',     '404', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:import', '#', 'admin', SYSDATE(), '', NULL, '');

-- ---- 业务管理 - 问诊测试 (405) ----
INSERT INTO sys_menu VALUES('405', '问诊测试', '4', '5', 'consultation-test', 'biz/consultation/ConsultationTest', '', '', 1, 0, 'C', '0', '0', 'biz:consultation:test', 'chat-dot-round', 'admin', SYSDATE(), '', NULL, '问诊测试菜单');


-- ----------------------------
-- 4、用户和角色关联表  用户N-1角色
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    role_id BIGINT(20) NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户和角色关联表';

INSERT INTO sys_user_role VALUES('1', '1');


-- ----------------------------
-- 5、角色和菜单关联表  角色1-N菜单
-- 注意: 此处为管理员(role_id=1)的系统菜单权限，
--       业务菜单权限在第五部分统一配置
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id BIGINT(20) NOT NULL COMMENT '角色ID',
    menu_id BIGINT(20) NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色和菜单关联表';

-- 医疗专家(role_id=2)的系统基础菜单权限（来自axzl.sql）
INSERT INTO sys_role_menu VALUES('2', '1');
INSERT INTO sys_role_menu VALUES('2', '2');
INSERT INTO sys_role_menu VALUES('2', '3');
INSERT INTO sys_role_menu VALUES('2', '100');
INSERT INTO sys_role_menu VALUES('2', '101');
INSERT INTO sys_role_menu VALUES('2', '102');
INSERT INTO sys_role_menu VALUES('2', '105');
INSERT INTO sys_role_menu VALUES('2', '106');
INSERT INTO sys_role_menu VALUES('2', '108');
INSERT INTO sys_role_menu VALUES('2', '109');
INSERT INTO sys_role_menu VALUES('2', '111');
INSERT INTO sys_role_menu VALUES('2', '112');
INSERT INTO sys_role_menu VALUES('2', '113');
INSERT INTO sys_role_menu VALUES('2', '114');
INSERT INTO sys_role_menu VALUES('2', '117');
INSERT INTO sys_role_menu VALUES('2', '118');
INSERT INTO sys_role_menu VALUES('2', '500');
INSERT INTO sys_role_menu VALUES('2', '501');
INSERT INTO sys_role_menu VALUES('2', '1000');
INSERT INTO sys_role_menu VALUES('2', '1001');
INSERT INTO sys_role_menu VALUES('2', '1002');
INSERT INTO sys_role_menu VALUES('2', '1003');
INSERT INTO sys_role_menu VALUES('2', '1004');
INSERT INTO sys_role_menu VALUES('2', '1005');
INSERT INTO sys_role_menu VALUES('2', '1006');
INSERT INTO sys_role_menu VALUES('2', '1007');
INSERT INTO sys_role_menu VALUES('2', '1008');
INSERT INTO sys_role_menu VALUES('2', '1009');
INSERT INTO sys_role_menu VALUES('2', '1010');
INSERT INTO sys_role_menu VALUES('2', '1011');
INSERT INTO sys_role_menu VALUES('2', '1012');
INSERT INTO sys_role_menu VALUES('2', '1013');
INSERT INTO sys_role_menu VALUES('2', '1014');
INSERT INTO sys_role_menu VALUES('2', '1015');
INSERT INTO sys_role_menu VALUES('2', '1025');
INSERT INTO sys_role_menu VALUES('2', '1026');
INSERT INTO sys_role_menu VALUES('2', '1027');
INSERT INTO sys_role_menu VALUES('2', '1028');
INSERT INTO sys_role_menu VALUES('2', '1029');
INSERT INTO sys_role_menu VALUES('2', '1030');
INSERT INTO sys_role_menu VALUES('2', '1031');
INSERT INTO sys_role_menu VALUES('2', '1032');
INSERT INTO sys_role_menu VALUES('2', '1033');
INSERT INTO sys_role_menu VALUES('2', '1034');
INSERT INTO sys_role_menu VALUES('2', '1039');
INSERT INTO sys_role_menu VALUES('2', '1040');
INSERT INTO sys_role_menu VALUES('2', '1041');
INSERT INTO sys_role_menu VALUES('2', '1042');
INSERT INTO sys_role_menu VALUES('2', '1043');
INSERT INTO sys_role_menu VALUES('2', '1044');
INSERT INTO sys_role_menu VALUES('2', '1045');
INSERT INTO sys_role_menu VALUES('2', '1046');
INSERT INTO sys_role_menu VALUES('2', '1047');
INSERT INTO sys_role_menu VALUES('2', '1048');
INSERT INTO sys_role_menu VALUES('2', '1050');
INSERT INTO sys_role_menu VALUES('2', '1051');
INSERT INTO sys_role_menu VALUES('2', '1052');
INSERT INTO sys_role_menu VALUES('2', '1053');
INSERT INTO sys_role_menu VALUES('2', '1054');
INSERT INTO sys_role_menu VALUES('2', '1055');


-- ----------------------------
-- 6、操作日志记录
-- ----------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
    oper_id        BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    title          VARCHAR(50)   DEFAULT ''              COMMENT '模块标题',
    business_type  INT(2)        DEFAULT 0               COMMENT '业务类型（0其它 1新增 2修改 3删除）',
    method         VARCHAR(200)  DEFAULT ''              COMMENT '方法名称',
    request_method VARCHAR(10)   DEFAULT ''              COMMENT '请求方式',
    operator_type  INT(1)        DEFAULT 0               COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      VARCHAR(50)   DEFAULT ''              COMMENT '操作人员',
    oper_url       VARCHAR(255)  DEFAULT ''              COMMENT '请求URL',
    oper_ip        VARCHAR(128)  DEFAULT ''              COMMENT '主机地址',
    oper_location  VARCHAR(255)  DEFAULT ''              COMMENT '操作地点',
    oper_param     VARCHAR(2000) DEFAULT ''              COMMENT '请求参数',
    json_result    VARCHAR(2000) DEFAULT ''              COMMENT '返回参数',
    status         INT(1)        DEFAULT 0               COMMENT '操作状态（0正常 1异常）',
    error_msg      VARCHAR(2000) DEFAULT ''              COMMENT '错误消息',
    oper_time      DATETIME                             COMMENT '操作时间',
    cost_time      BIGINT(20)    DEFAULT 0               COMMENT '消耗时间',
    PRIMARY KEY (oper_id),
    KEY idx_sys_oper_log_bt (business_type),
    KEY idx_sys_oper_log_s  (status),
    KEY idx_sys_oper_log_ot (oper_time)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='操作日志记录';


-- ----------------------------
-- 7、字典类型表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
    dict_id     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '字典主键',
    dict_name   VARCHAR(100) DEFAULT ''              COMMENT '字典名称',
    dict_type   VARCHAR(100) DEFAULT ''              COMMENT '字典类型',
    status      CHAR(1)      DEFAULT '0'             COMMENT '状态（0正常 1停用）',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME                             COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME                             COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (dict_id),
    UNIQUE (dict_type)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='字典类型表';

INSERT INTO sys_dict_type VALUES(1,  '用户性别', 'sys_user_sex',       '0', 'admin', SYSDATE(), '', NULL, '用户性别列表');
INSERT INTO sys_dict_type VALUES(2,  '菜单状态', 'sys_show_hide',      '0', 'admin', SYSDATE(), '', NULL, '菜单状态列表');
INSERT INTO sys_dict_type VALUES(3,  '系统开关', 'sys_normal_disable', '0', 'admin', SYSDATE(), '', NULL, '系统开关列表');
INSERT INTO sys_dict_type VALUES(6,  '系统是否', 'sys_yes_no',         '0', 'admin', SYSDATE(), '', NULL, '系统是否列表');
INSERT INTO sys_dict_type VALUES(9,  '操作类型', 'sys_oper_type',      '0', 'admin', SYSDATE(), '', NULL, '操作类型列表');
INSERT INTO sys_dict_type VALUES(10, '系统状态', 'sys_common_status',  '0', 'admin', SYSDATE(), '', NULL, '登录状态列表');


-- ----------------------------
-- 8、字典数据表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data (
    dict_code   BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '字典编码',
    dict_sort   INT(4)       DEFAULT 0               COMMENT '字典排序',
    dict_label  VARCHAR(100) DEFAULT ''              COMMENT '字典标签',
    dict_value  VARCHAR(100) DEFAULT ''              COMMENT '字典键值',
    dict_type   VARCHAR(100) DEFAULT ''              COMMENT '字典类型',
    css_class   VARCHAR(100) DEFAULT NULL            COMMENT '样式属性（其他样式扩展）',
    list_class  VARCHAR(100) DEFAULT NULL            COMMENT '表格回显样式',
    is_default  CHAR(1)      DEFAULT 'N'             COMMENT '是否默认（Y是 N否）',
    status      CHAR(1)      DEFAULT '0'             COMMENT '状态（0正常 1停用）',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME                             COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME                             COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (dict_code)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='字典数据表';

INSERT INTO sys_dict_data VALUES(1,  1,  '男',       '0',       'sys_user_sex',       '',   '',        'Y', '0', 'admin', SYSDATE(), '', NULL, '性别男');
INSERT INTO sys_dict_data VALUES(2,  2,  '女',       '1',       'sys_user_sex',       '',   '',        'N', '0', 'admin', SYSDATE(), '', NULL, '性别女');
INSERT INTO sys_dict_data VALUES(3,  3,  '未知',     '2',       'sys_user_sex',       '',   '',        'N', '0', 'admin', SYSDATE(), '', NULL, '性别未知');
INSERT INTO sys_dict_data VALUES(4,  1,  '显示',     '0',       'sys_show_hide',      '',   'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '显示菜单');
INSERT INTO sys_dict_data VALUES(5,  2,  '隐藏',     '1',       'sys_show_hide',      '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '隐藏菜单');
INSERT INTO sys_dict_data VALUES(6,  1,  '正常',     '0',       'sys_normal_disable', '',   'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '正常状态');
INSERT INTO sys_dict_data VALUES(7,  2,  '停用',     '1',       'sys_normal_disable', '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '停用状态');
INSERT INTO sys_dict_data VALUES(8,  1,  '正常',     '0',       'sys_job_status',     '',   'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '正常状态');
INSERT INTO sys_dict_data VALUES(9,  2,  '暂停',     '1',       'sys_job_status',     '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '停用状态');
INSERT INTO sys_dict_data VALUES(10, 1,  '默认',     'DEFAULT', 'sys_job_group',      '',   '',        'Y', '0', 'admin', SYSDATE(), '', NULL, '默认分组');
INSERT INTO sys_dict_data VALUES(11, 2,  '系统',     'SYSTEM',  'sys_job_group',      '',   '',        'N', '0', 'admin', SYSDATE(), '', NULL, '系统分组');
INSERT INTO sys_dict_data VALUES(12, 1,  '是',       'Y',       'sys_yes_no',         '',   'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '系统默认是');
INSERT INTO sys_dict_data VALUES(13, 2,  '否',       'N',       'sys_yes_no',         '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '系统默认否');
INSERT INTO sys_dict_data VALUES(16, 1,  '正常',     '0',       'sys_notice_status',  '',   'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '正常状态');
INSERT INTO sys_dict_data VALUES(17, 2,  '关闭',     '1',       'sys_notice_status',  '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '关闭状态');
INSERT INTO sys_dict_data VALUES(18, 99, '其他',     '0',       'sys_oper_type',      '',   'info',    'N', '0', 'admin', SYSDATE(), '', NULL, '其他操作');
INSERT INTO sys_dict_data VALUES(19, 1,  '新增',     '1',       'sys_oper_type',      '',   'info',    'N', '0', 'admin', SYSDATE(), '', NULL, '新增操作');
INSERT INTO sys_dict_data VALUES(20, 2,  '修改',     '2',       'sys_oper_type',      '',   'info',    'N', '0', 'admin', SYSDATE(), '', NULL, '修改操作');
INSERT INTO sys_dict_data VALUES(21, 3,  '删除',     '3',       'sys_oper_type',      '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '删除操作');
INSERT INTO sys_dict_data VALUES(22, 4,  '授权',     '4',       'sys_oper_type',      '',   'primary', 'N', '0', 'admin', SYSDATE(), '', NULL, '授权操作');
INSERT INTO sys_dict_data VALUES(23, 5,  '导出',     '5',       'sys_oper_type',      '',   'warning', 'N', '0', 'admin', SYSDATE(), '', NULL, '导出操作');
INSERT INTO sys_dict_data VALUES(24, 6,  '导入',     '6',       'sys_oper_type',      '',   'warning', 'N', '0', 'admin', SYSDATE(), '', NULL, '导入操作');
INSERT INTO sys_dict_data VALUES(25, 7,  '强退',     '7',       'sys_oper_type',      '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '强退操作');
INSERT INTO sys_dict_data VALUES(27, 9,  '清空数据', '9',       'sys_oper_type',      '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '清空操作');
INSERT INTO sys_dict_data VALUES(28, 1,  '成功',     '0',       'sys_common_status',  '',   'primary', 'N', '0', 'admin', SYSDATE(), '', NULL, '正常状态');
INSERT INTO sys_dict_data VALUES(29, 2,  '失败',     '1',       'sys_common_status',  '',   'danger',  'N', '0', 'admin', SYSDATE(), '', NULL, '停用状态');


-- ----------------------------
-- 9、参数配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    config_id    INT(5)       NOT NULL AUTO_INCREMENT COMMENT '参数主键',
    config_name  VARCHAR(100) DEFAULT ''              COMMENT '参数名称',
    config_key   VARCHAR(100) DEFAULT ''              COMMENT '参数键名',
    config_value VARCHAR(500) DEFAULT ''              COMMENT '参数键值',
    config_type  CHAR(1)      DEFAULT 'N'             COMMENT '系统内置（Y是 N否）',
    create_by    VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time  DATETIME                             COMMENT '创建时间',
    update_by    VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time  DATETIME                             COMMENT '更新时间',
    remark       VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (config_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='参数配置表';

INSERT INTO sys_config VALUES(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',               'skin-blue',  'Y', 'admin', SYSDATE(), '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO sys_config VALUES(2, '用户管理-账号初始密码',         'sys.user.initPassword',            '123456',     'Y', 'admin', SYSDATE(), '', NULL, '初始化密码 123456');
INSERT INTO sys_config VALUES(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark', 'Y', 'admin', SYSDATE(), '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO sys_config VALUES(4, '账号自助-验证码开关',           'sys.account.captchaEnabled',       'false',       'Y', 'admin', SYSDATE(), '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO sys_config VALUES(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'true',      'Y', 'admin', SYSDATE(), '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO sys_config VALUES(6, '用户登录-黑名单列表',           'sys.login.blackIPList',            '',           'Y', 'admin', SYSDATE(), '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO sys_config VALUES(7, '用户管理-初始密码修改策略',     'sys.account.initPasswordModify',   '1',          'Y', 'admin', SYSDATE(), '', NULL, '0：初始密码修改策略关闭，1：提醒用户修改初始密码');
INSERT INTO sys_config VALUES(8, '用户管理-账号密码更新周期',     'sys.account.passwordValidateDays', '0',          'Y', 'admin', SYSDATE(), '', NULL, '密码更新周期（0不限制，>0且<365则提醒修改）');


-- ----------------------------
-- 10、系统访问记录
-- ----------------------------
DROP TABLE IF EXISTS sys_logininfor;
CREATE TABLE sys_logininfor (
    info_id        BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '访问ID',
    user_name      VARCHAR(50) DEFAULT ''              COMMENT '用户账号',
    ipaddr         VARCHAR(128) DEFAULT ''             COMMENT '登录IP地址',
    login_location VARCHAR(255) DEFAULT ''             COMMENT '登录地点',
    browser        VARCHAR(50)  DEFAULT ''             COMMENT '浏览器类型',
    os             VARCHAR(50)  DEFAULT ''             COMMENT '操作系统',
    status         CHAR(1)     DEFAULT '0'             COMMENT '登录状态（0成功 1失败）',
    msg            VARCHAR(255) DEFAULT ''             COMMENT '提示消息',
    login_time     DATETIME                            COMMENT '访问时间',
    PRIMARY KEY (info_id),
    KEY idx_sys_logininfor_s  (status),
    KEY idx_sys_logininfor_lt (login_time)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='系统访问记录';


-- ============================================================================
-- 第三部分: 业务数据表
-- 包含: 知识库元数据、上传文档、问诊会话、对话消息、用户健康档案
-- ============================================================================

-- ----------------------------
-- 1、知识库元数据表
-- ----------------------------
DROP TABLE IF EXISTS biz_knowledge_meta;
CREATE TABLE biz_knowledge_meta (
    id               BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '知识ID',
    question         TEXT         NOT NULL                COMMENT '问题/文本块内容',
    answer           TEXT                                COMMENT '答案/补充内容',
    label            VARCHAR(50)  DEFAULT ''              COMMENT '分类标签',
    score            INT(11)      DEFAULT 0               COMMENT '评分/重要度',
    related_diseases VARCHAR(500) DEFAULT ''              COMMENT '关联疾病',
    vector_id        VARCHAR(100) DEFAULT ''              COMMENT 'Milvus向量ID',
    vector_status    CHAR(1)      DEFAULT '0'             COMMENT '向量状态(0未导入 1已导入 2失败)',
    source_type      VARCHAR(20)  DEFAULT 'qa'            COMMENT '来源类型(qa/doc)',
    source_id        BIGINT(20)   DEFAULT NULL            COMMENT '来源文档ID',
    chunk_index      INT(11)      DEFAULT 0               COMMENT '分块序号',
    source           VARCHAR(100) DEFAULT ''              COMMENT '数据来源',
    status           CHAR(1)      DEFAULT '0'             COMMENT '状态(0正常 1停用)',
    create_by        VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time      DATETIME                             COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time      DATETIME                             COMMENT '更新时间',
    del_flag         CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
    remark           VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_vector_id    (vector_id),
    KEY idx_label        (label),
    KEY idx_source_type  (source_type)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='知识库元数据表';


-- ----------------------------
-- 2、上传文档表
-- ----------------------------
DROP TABLE IF EXISTS biz_document;
CREATE TABLE biz_document (
    id                BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    file_name         VARCHAR(200) NOT NULL                COMMENT '原始文件名',
    file_type         VARCHAR(20)  NOT NULL                COMMENT '文件类型(txt/word/pdf/md)',
    file_size         BIGINT(20)   DEFAULT 0               COMMENT '文件大小(字节)',
    milvus_file_path  VARCHAR(500) DEFAULT ''              COMMENT 'Milvus对象存储路径',
    extracted_content LONGTEXT                             COMMENT '提取的文本内容',
    chunk_count       INT(11)      DEFAULT 0               COMMENT '切分块数',
    vector_status     CHAR(1)      DEFAULT '0'             COMMENT '向量状态(0未处理 1处理中 2已完成 3失败)',
    upload_by         BIGINT(20)   NOT NULL                COMMENT '上传用户ID',
    status            CHAR(1)      DEFAULT '0'             COMMENT '状态(0正常 1删除)',
    create_time       DATETIME                             COMMENT '上传时间',
    update_time       DATETIME                             COMMENT '更新时间',
    del_flag          CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
    remark            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_upload_by     (upload_by),
    KEY idx_vector_status (vector_status)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='上传文档表';


-- ----------------------------
-- 3、问诊会话表
-- 注意: is_pinned 字段支持会话置顶功能
-- ----------------------------
DROP TABLE IF EXISTS biz_chat_session;
CREATE TABLE biz_chat_session (
    session_id        VARCHAR(64)  NOT NULL                COMMENT '会话ID(UUID)',
    user_id           BIGINT(20)   NOT NULL                COMMENT '用户ID',
    title             VARCHAR(200) DEFAULT ''              COMMENT '会话标题',
    symptom_summary   TEXT                                COMMENT '症状摘要',
    ai_diagnosis      TEXT                                COMMENT 'AI诊断摘要',
    recommended_label VARCHAR(50)  DEFAULT ''              COMMENT '推荐科室标签',
    message_count     INT(11)      DEFAULT 0               COMMENT '消息数量',
    satisfaction      TINYINT(1)   DEFAULT NULL            COMMENT '满意度(1-5星)',
    tokens_used       INT(11)      DEFAULT 0               COMMENT '消耗Token数',
    last_message_time DATETIME                             COMMENT '最后消息时间',
    status            CHAR(1)      DEFAULT '0'             COMMENT '状态(0进行中 1已结束)',
    create_time       DATETIME                             COMMENT '创建时间',
    update_time       DATETIME                             COMMENT '更新时间',
    del_flag          CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
    remark            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    is_pinned         TINYINT(1)   DEFAULT 0               COMMENT '是否置顶(0否 1是)',
    PRIMARY KEY (session_id),
    KEY idx_user_id      (user_id),
    KEY idx_create_time  (create_time)
) ENGINE=InnoDB COMMENT='问诊会话表';


-- ----------------------------
-- 4、对话消息表
-- ----------------------------
DROP TABLE IF EXISTS biz_chat_message;
CREATE TABLE biz_chat_message (
    message_id        BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    session_id        VARCHAR(64) NOT NULL                COMMENT '会话ID',
    user_id           BIGINT(20)  NOT NULL                COMMENT '用户ID',
    role              VARCHAR(20) NOT NULL                COMMENT '角色(user/assistant)',
    content           TEXT        NOT NULL                COMMENT '消息内容',
    retrieval_results JSON                                COMMENT '向量检索结果(Top5)',
    create_time       DATETIME                            COMMENT '创建时间',
    PRIMARY KEY (message_id),
    KEY idx_session_id  (session_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='对话消息表';


-- ----------------------------
-- 5、用户健康档案表
-- ----------------------------
DROP TABLE IF EXISTS biz_user_profile;
CREATE TABLE biz_user_profile (
    profile_id      BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '档案ID',
    user_id         BIGINT(20)   NOT NULL                COMMENT '用户ID',
    birthday        DATE                                COMMENT '出生日期',
    gender          CHAR(1)      DEFAULT '0'             COMMENT '性别(0男 1女 2未知)',
    blood_type      VARCHAR(10)  DEFAULT ''              COMMENT '血型',
    height          DECIMAL(5,1) DEFAULT NULL            COMMENT '身高(cm)',
    weight          DECIMAL(5,1) DEFAULT NULL            COMMENT '体重(kg)',
    allergy_history TEXT                                COMMENT '过敏史',
    medical_history TEXT                                COMMENT '既往病史',
    family_history  TEXT                                COMMENT '家族病史',
    lifestyle       TEXT                                COMMENT '生活习惯',
    status          CHAR(1)      DEFAULT '0'             COMMENT '状态(0正常 1停用)',
    create_time     DATETIME                             COMMENT '创建时间',
    update_time     DATETIME                             COMMENT '更新时间',
    del_flag        CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
    remark          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (profile_id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='用户健康档案表';


-- ============================================================================
-- 第四部分: AI模型配置表
-- 支持动态管理不同AI提供商的模型参数，无需重启即可切换
-- ============================================================================

DROP TABLE IF EXISTS biz_ai_model_config;
CREATE TABLE biz_ai_model_config (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    provider        VARCHAR(50)   NOT NULL DEFAULT ''     COMMENT '提供商标识(如openai/deepseek/anthropic/localhost)',
    model_name      VARCHAR(150)  NOT NULL               COMMENT '模型名称',
    api_base_url    VARCHAR(500)  NOT NULL DEFAULT ''     COMMENT 'API基础URL',
    api_key         VARBINARY(2048) NOT NULL DEFAULT ''   COMMENT 'API密钥(加密存储)',
    model_type      VARCHAR(20)   NOT NULL DEFAULT 'text' COMMENT '模型类型(text/embedding/image/audio)',
    temperature     DECIMAL(3,2)  NOT NULL DEFAULT 0.1   COMMENT '温度参数(0-2)',
    max_tokens      INT           NOT NULL DEFAULT 4096  COMMENT '最大生成Token数',
    top_p           DECIMAL(3,2)  NOT NULL DEFAULT 0.3   COMMENT 'Top-P采样参数',
    top_k           INT           NOT NULL DEFAULT 0     COMMENT 'Top-K采样参数(0=不限制)',
    frequency_penalty DECIMAL(3,2) NOT NULL DEFAULT 0.2  COMMENT '频率惩罚(-2.0至2.0)',
    presence_penalty  DECIMAL(3,2) NOT NULL DEFAULT 0.1  COMMENT '存在惩罚(-2.0至2.0)',
    stop_sequences  JSON                   DEFAULT NULL  COMMENT '停止序列(JSON数组)',
    is_default      TINYINT(1)    NOT NULL DEFAULT 0     COMMENT '是否默认模型(0否 1是)',
    sort_order      INT           NOT NULL DEFAULT 0     COMMENT '显示排序',
    config_extra    JSON                   DEFAULT NULL  COMMENT '额外配置(如{"seed":123,"responseFormat":"json_object"})',
    status          TINYINT(1)    NOT NULL DEFAULT 1     COMMENT '状态(0停用 1启用)',
    create_by       VARCHAR(64)   NOT NULL DEFAULT ''    COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       VARCHAR(64)   NOT NULL DEFAULT ''    COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500)           DEFAULT NULL  COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_model_name (model_name),
    KEY idx_provider        (provider),
    KEY idx_model_type      (model_type),
    KEY idx_status_default  (status, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=100 COMMENT='AI大模型配置表';

-- AI模型初始数据（医疗场景专用）
-- 注意: api_key 为十六进制编码的占位符，生产环境请替换为真实密钥
INSERT INTO biz_ai_model_config
    (id, provider, model_name, api_base_url, api_key, model_type, temperature, max_tokens, top_p, is_default, status, create_time, update_time)
VALUES
    -- DeepSeek V4-Flash: 医疗场景主力默认模型（高性价比+长上下文）
    (100, 'deepseek', 'deepseek-v4-flash', 'https://api.deepseek.com',
     UNHEX('446565705365656B456D6265644D6564'), 'text', 0.30, 128000, 0.80, 1, 1, NOW(), NOW()),

    -- DeepSeek V4-Pro: 医疗高阶推理（疑难病例/文献分析）
    (101, 'deepseek', 'deepseek-v4-pro', 'https://api.deepseek.com',
     UNHEX('446565705365656B456D6265644D6564'), 'text', 0.20, 128000, 0.70, 0, 1, NOW(), NOW()),

    -- 本地嵌入模型 bge-large-zh-v1.5: 医疗知识库向量检索（OpenAI兼容接口）
    (102, 'localhost', 'bge-large-zh-v1.5', 'http://localhost:8718',
     UNHEX('736B2D787878'), 'embedding', 0.00, 8192, 1.00, 1, 1, NOW(), NOW()),

    -- OpenAI GPT-4o: 医疗多模态（影像报告解读/复杂病历）
    (103, 'openai', 'gpt-4o-2024-11-20', 'https://api.openai.com/v1',
     UNHEX('4F70656E4149475054346F4D6564'), 'text', 0.40, 16384, 0.90, 0, 1, NOW(), NOW()),

    -- OpenAI Embedding Large: 医疗文献/指南高维向量
    (104, 'openai', 'text-embedding-3-large', 'https://api.openai.com/v1',
     UNHEX('4F70656E4149456D6265644D6564'), 'embedding', 0.00, 8191, 1.00, 0, 1, NOW(), NOW()),

    -- Anthropic Claude 3.7 Sonnet: 超长医疗文档（住院病历/基因测序报告）
    (105, 'anthropic', 'claude-3-7-sonnet-20240229', 'https://api.anthropic.com/v1',
     UNHEX('416E7468726F706963436C617564654D6564'), 'text', 0.30, 200000, 0.95, 0, 1, NOW(), NOW());


-- ============================================================================
-- 第五部分: 角色权限分配
-- 说明: INSERT IGNORE 防止重复插入导致主键冲突
-- ============================================================================

-- ---- 超级管理员(role_id=1): 拥有所有业务权限 ----
-- 业务管理目录
INSERT IGNORE INTO sys_role_menu VALUES('1', '4');
-- 知识库管理
INSERT IGNORE INTO sys_role_menu VALUES('1', '401');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4001');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4002');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4003');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4004');
-- 文档管理
INSERT IGNORE INTO sys_role_menu VALUES('1', '402');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4005');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4006');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4018');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4019');
-- 问诊记录（完整权限）
INSERT IGNORE INTO sys_role_menu VALUES('1', '403');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4007');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4008');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4013');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4014');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4015');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4016');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4017');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4020');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4021');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4022');
-- 健康档案管理
INSERT IGNORE INTO sys_role_menu VALUES('1', '404');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4009');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4010');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4011');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4012');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4023');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4024');
-- 问诊测试
INSERT IGNORE INTO sys_role_menu VALUES('1', '405');
-- AI模型管理
INSERT IGNORE INTO sys_role_menu VALUES('1', '118');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1050');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1051');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1052');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1053');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1054');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1055');


-- ---- 医疗专家(role_id=2): 知识库/文档/问诊/档案完整权限 ----
-- 业务管理目录
INSERT IGNORE INTO sys_role_menu VALUES('2', '4');
-- 知识库管理（完整权限）
INSERT IGNORE INTO sys_role_menu VALUES('2', '401');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4001');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4002');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4003');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4004');
-- 文档管理（完整权限）
INSERT IGNORE INTO sys_role_menu VALUES('2', '402');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4005');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4006');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4018');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4019');
-- 问诊记录（查询/删除/编辑/置顶/流式对话/历史/详情/重命名）
INSERT IGNORE INTO sys_role_menu VALUES('2', '403');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4007');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4008');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4013');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4014');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4015');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4016');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4017');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4020');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4021');
-- 健康档案管理（完整权限）
INSERT IGNORE INTO sys_role_menu VALUES('2', '404');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4009');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4010');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4011');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4012');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4023');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4024');
-- 问诊测试
INSERT IGNORE INTO sys_role_menu VALUES('2', '405');
-- AI模型管理（查询/查看/切换）
INSERT IGNORE INTO sys_role_menu VALUES('2', '118');
INSERT IGNORE INTO sys_role_menu VALUES('2', '1050');
INSERT IGNORE INTO sys_role_menu VALUES('2', '1055');


-- ---- 普通医生(role_id=3): 知识库查询 + 问诊/档案操作 ----
-- 业务管理目录
INSERT IGNORE INTO sys_role_menu VALUES('3', '4');
-- 知识库管理（仅查询）
INSERT IGNORE INTO sys_role_menu VALUES('3', '401');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4001');
-- 问诊记录（查询/编辑/置顶/流式对话/历史/详情/满意度）
INSERT IGNORE INTO sys_role_menu VALUES('3', '403');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4007');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4013');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4014');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4015');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4016');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4017');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4020');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4021');
-- 健康档案管理（完整权限）
INSERT IGNORE INTO sys_role_menu VALUES('3', '404');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4009');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4010');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4011');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4012');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4023');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4024');
-- 问诊测试
INSERT IGNORE INTO sys_role_menu VALUES('3', '405');


-- ---- 护士助理(role_id=4): 问诊查询 + 档案查询 ----
-- 业务管理目录
INSERT IGNORE INTO sys_role_menu VALUES('4', '4');
-- 问诊记录（查询/流式对话/历史/详情）
INSERT IGNORE INTO sys_role_menu VALUES('4', '403');
INSERT IGNORE INTO sys_role_menu VALUES('4', '4007');
INSERT IGNORE INTO sys_role_menu VALUES('4', '4013');
INSERT IGNORE INTO sys_role_menu VALUES('4', '4014');
INSERT IGNORE INTO sys_role_menu VALUES('4', '4015');
-- 健康档案管理（仅查询）
INSERT IGNORE INTO sys_role_menu VALUES('4', '404');
INSERT IGNORE INTO sys_role_menu VALUES('4', '4011');
-- 问诊测试
INSERT IGNORE INTO sys_role_menu VALUES('4', '405');


-- ---- 患者(role_id=5): 问诊测试 + 个人档案 ----
-- 业务管理目录
INSERT IGNORE INTO sys_role_menu VALUES('5', '4');
-- 问诊测试（主要功能入口）
INSERT IGNORE INTO sys_role_menu VALUES('5', '405');
-- 问诊相关权限（流式对话/历史/详情/删除/编辑/置顶/满意度）
INSERT IGNORE INTO sys_role_menu VALUES('5', '403');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4007');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4008');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4013');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4014');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4015');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4016');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4020');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4021');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4022');
-- 健康档案管理（增删改查）
INSERT IGNORE INTO sys_role_menu VALUES('5', '404');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4009');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4010');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4011');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4012');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4023');
INSERT IGNORE INTO sys_role_menu VALUES('5', '4024');


-- ============================================================================
-- 第六部分: 测试数据
-- 说明: 所有测试用户默认密码为 admin123
--       BCrypt哈希: $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
-- ============================================================================

-- ---- 6.1 测试用户 ----
-- 管理员 (user_id=1, role_id=1) - 已在第二部分创建
INSERT IGNORE INTO sys_user_role VALUES(1, 1);

-- 医疗专家 (role_id=2)
INSERT IGNORE INTO sys_user VALUES(10, 'expert_zhang', '张教授', '00', 'zhang.expert@hospital.com', '13800001001', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '心血管内科专家');
INSERT IGNORE INTO sys_user VALUES(11, 'expert_li',   '李主任', '00', 'li.expert@hospital.com',    '13800001002', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '呼吸内科专家');
INSERT IGNORE INTO sys_user VALUES(12, 'expert_wang', '王博士', '00', 'wang.expert@hospital.com',  '13800001003', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '消化内科专家');

-- 普通医生 (role_id=3)
INSERT IGNORE INTO sys_user VALUES(20, 'doctor_chen',  '陈医生', '00', 'chen.doctor@hospital.com',  '13800002001', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '全科医生');
INSERT IGNORE INTO sys_user VALUES(21, 'doctor_liu',   '刘医师', '00', 'liu.doctor@hospital.com',   '13800002002', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '儿科医生');
INSERT IGNORE INTO sys_user VALUES(22, 'doctor_zhao',  '赵医生', '00', 'zhao.doctor@hospital.com',  '13800002003', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '骨科医生');
INSERT IGNORE INTO sys_user VALUES(23, 'doctor_sun',   '孙医师', '00', 'sun.doctor@hospital.com',   '13800002004', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '皮肤科医生');
INSERT IGNORE INTO sys_user VALUES(24, 'doctor_zhou',  '周医生', '00', 'zhou.doctor@hospital.com',  '13800002005', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '神经内科医生');

-- 护士助理 (role_id=4)
INSERT IGNORE INTO sys_user VALUES(30, 'nurse_wu',      '吴护士', '00', 'wu.nurse@hospital.com',      '13800003001', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '内科护士');
INSERT IGNORE INTO sys_user VALUES(31, 'nurse_zheng',   '郑护士', '00', 'zheng.nurse@hospital.com',   '13800003002', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '外科护士');
INSERT IGNORE INTO sys_user VALUES(32, 'assistant_feng', '冯助理', '00', 'feng.assistant@hospital.com', '13800003003', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '医疗助理');

-- 患者 (role_id=5)
INSERT IGNORE INTO sys_user VALUES(40, 'patient_huang', '黄小明', '00', 'huang.patient@email.com', '13800004001', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者1');
INSERT IGNORE INTO sys_user VALUES(41, 'patient_xu',   '徐小红', '00', 'xu.patient@email.com',   '13800004002', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者2');
INSERT IGNORE INTO sys_user VALUES(42, 'patient_gao',  '高大伟', '00', 'gao.patient@email.com',  '13800004003', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者3');
INSERT IGNORE INTO sys_user VALUES(43, 'patient_lin',  '林小芳', '00', 'lin.patient@email.com',  '13800004004', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者4');
INSERT IGNORE INTO sys_user VALUES(44, 'patient_he',   '何建国', '00', 'he.patient@email.com',   '13800004005', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者5');
INSERT IGNORE INTO sys_user VALUES(45, 'patient_luo',  '罗美玲', '00', 'luo.patient@email.com',  '13800004006', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者6');
INSERT IGNORE INTO sys_user VALUES(46, 'patient_song', '宋志强', '00', 'song.patient@email.com', '13800004007', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者7');
INSERT IGNORE INTO sys_user VALUES(47, 'patient_tang', '唐丽娟', '00', 'tang.patient@email.com', '13800004008', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者8');
INSERT IGNORE INTO sys_user VALUES(48, 'patient_deng', '邓伟强', '00', 'deng.patient@email.com', '13800004009', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者9');
INSERT IGNORE INTO sys_user VALUES(49, 'patient_xie',  '谢晓燕', '00', 'xie.patient@email.com',  '13800004010', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者10');

-- 用户角色关联
INSERT IGNORE INTO sys_user_role VALUES(10, 2);
INSERT IGNORE INTO sys_user_role VALUES(11, 2);
INSERT IGNORE INTO sys_user_role VALUES(12, 2);
INSERT IGNORE INTO sys_user_role VALUES(20, 3);
INSERT IGNORE INTO sys_user_role VALUES(21, 3);
INSERT IGNORE INTO sys_user_role VALUES(22, 3);
INSERT IGNORE INTO sys_user_role VALUES(23, 3);
INSERT IGNORE INTO sys_user_role VALUES(24, 3);
INSERT IGNORE INTO sys_user_role VALUES(30, 4);
INSERT IGNORE INTO sys_user_role VALUES(31, 4);
INSERT IGNORE INTO sys_user_role VALUES(32, 4);
INSERT IGNORE INTO sys_user_role VALUES(40, 5);
INSERT IGNORE INTO sys_user_role VALUES(41, 5);
INSERT IGNORE INTO sys_user_role VALUES(42, 5);
INSERT IGNORE INTO sys_user_role VALUES(43, 5);
INSERT IGNORE INTO sys_user_role VALUES(44, 5);
INSERT IGNORE INTO sys_user_role VALUES(45, 5);
INSERT IGNORE INTO sys_user_role VALUES(46, 5);
INSERT IGNORE INTO sys_user_role VALUES(47, 5);
INSERT IGNORE INTO sys_user_role VALUES(48, 5);
INSERT IGNORE INTO sys_user_role VALUES(49, 5);


-- ---- 6.2 用户健康档案测试数据 ----
INSERT INTO biz_user_profile (user_id, birthday, gender, blood_type, height, weight, allergy_history, medical_history, family_history, lifestyle, status, create_time, update_time, del_flag, remark)
VALUES
    (1,  '1990-05-12', '0', 'A',  175.5, 72.3, '青霉素过敏',    '无',             '母亲高血压',   '偶尔吸烟，每周运动2次', '0', '2024-01-10 08:30:00', '2025-04-20 14:25:00', '0', '体检正常'),
    (2,  '1985-11-23', '1', 'B',  162.0, 55.8, '花粉过敏',      '甲状腺功能减退', '父亲糖尿病',   '不吸烟，规律瑜伽',       '0', '2024-02-15 10:15:00', '2025-04-22 09:10:00', '0', '需定期复查甲功'),
    (3,  '2000-07-08', '0', 'O',  180.2, 80.5, '无',            '无',             '无',           '运动员，每天锻炼',       '0', '2024-03-20 12:00:00', '2025-04-18 16:45:00', '0', '身体健康'),
    (4,  '1978-12-01', '1', 'AB', 160.0, 68.0, '海鲜过敏',      '高血压，高血脂', '母亲冠心病',   '不吸烟，偶尔饮酒',       '0', '2024-01-05 09:00:00', '2025-04-21 11:20:00', '0', '长期服药控制血压'),
    (5,  '1995-03-19', '0', 'A',  170.0, 65.2, '无',            '哮喘',           '父亲哮喘',     '吸烟10年，不饮酒',       '0', '2024-04-10 14:30:00', '2025-04-19 13:40:00', '0', '哮喘控制良好'),
    (6,  '1982-09-25', '1', 'B',  165.5, 71.0, '无',            '2型糖尿病',      '祖母糖尿病',   '规律作息，不烟酒',       '0', '2024-05-18 11:00:00', '2025-04-23 08:00:00', '0', '血糖控制中'),
    (7,  '2005-04-14', '0', 'O',  172.0, 60.0, '尘螨过敏',      '无',             '无',           '学生，久坐少动',         '0', '2024-06-22 09:45:00', '2025-04-24 15:30:00', '0', '建议增加运动'),
    (8,  '1970-06-30', '1', 'AB', 158.0, 52.0, '无',            '骨质疏松',       '母亲骨质疏松', '不吸烟，每日散步',       '0', '2024-07-11 13:20:00', '2025-04-20 10:10:00', '0', '补充钙剂'),
    (9,  '1998-10-17', '0', 'A',  185.0, 85.0, '磺胺类药物过敏', '无',             '无',           '健身爱好者，高蛋白饮食', '0', '2024-08-05 16:00:00', '2025-04-22 14:00:00', '0', '体型健硕'),
    (10, '1988-02-29', '2', 'O',  168.0, 62.5, '无',            '慢性胃炎',       '父亲胃癌',     '喜辛辣，三餐不规律',     '0', '2024-09-19 08:00:00', '2025-04-25 09:30:00', '0', '建议胃镜检查');


-- ---- 6.3 问诊会话测试数据 ----
INSERT INTO biz_chat_session (session_id, user_id, title, symptom_summary, ai_diagnosis, recommended_label, message_count, satisfaction, tokens_used, last_message_time, status, create_time, update_time, del_flag, remark)
VALUES
    ('11111111-1111-1111-1111-111111111111', 1, '感冒咳嗽问诊',
     '患者三天前开始流涕、咽痛，今日出现干咳，无发热',
     '急性上呼吸道感染（普通感冒）可能性大', '呼吸内科',
     4, 4, 350, '2025-04-20 10:30:00', '1',
     '2025-04-20 09:00:00', '2025-04-20 10:35:00', '0', '患者对回复满意'),

    ('22222222-2222-2222-2222-222222222222', 2, '胃痛咨询',
     '上腹部隐痛一周，餐后加重，伴反酸嗳气，无黑便',
     '功能性消化不良或慢性胃炎可能，建议完善胃镜检查', '消化内科',
     2, NULL, 210, '2025-04-22 15:20:00', '0',
     '2025-04-22 14:50:00', '2025-04-22 15:25:00', '0', '会话进行中，等待用户追问'),

    ('33333333-3333-3333-3333-333333333333', 3, '高血压用药咨询',
     '高血压病史5年，目前服用硝苯地平控释片，血压控制欠佳，收缩压150左右',
     '血压未达标，建议联合使用普利类或沙坦类药物，需心内科进一步调整方案', '心血管内科',
     5, 5, 520, '2025-04-24 11:45:00', '1',
     '2025-04-24 10:00:00', '2025-04-24 11:50:00', '0', '患者评价很好');


-- ---- 6.4 对话消息测试数据 ----
INSERT INTO biz_chat_message (message_id, session_id, user_id, role, content, retrieval_results, create_time)
VALUES
-- 会话1: 感冒咳嗽问诊
(100, '11111111-1111-1111-1111-111111111111', 1, 'user',
 '医生您好，我最近三天开始流鼻涕、嗓子疼，今天开始干咳，没有发烧，请问该吃什么药？',
 '[{"doc_id":"D1001","title":"普通感冒诊疗指南","score":0.92,"snippet":"普通感冒以对症治疗为主，无发热时无需使用抗生素..."}]',
 '2025-04-20 09:00:00'),

(101, '11111111-1111-1111-1111-111111111111', 1, 'assistant',
 '根据您描述的症状（流涕、咽痛、干咳、无发热），首先考虑普通感冒。建议：多休息、多饮水；可使用含氯苯那敏的复方感冒药缓解症状；干咳明显时可用右美沙芬。如症状持续>7天或出现发热、呼吸困难，请及时就医。',
 '[{"doc_id":"D1002","title":"咳嗽的诊疗流程","score":0.88,"snippet":"干咳可选用右美沙芬，需排除哮喘等病因..."}]',
 '2025-04-20 09:05:00'),

(102, '11111111-1111-1111-1111-111111111111', 1, 'user',
 '谢谢医生，我家里有复方氨酚烷胺片，可以吃吗？另外需要吃消炎药吗？',
 NULL,
 '2025-04-20 09:12:00'),

(103, '11111111-1111-1111-1111-111111111111', 1, 'assistant',
 '复方氨酚烷胺片可以缓解感冒症状，按说明书服用即可。普通感冒多为病毒性，不建议自行服用"消炎药"（即抗生素），除非合并细菌感染证据（如黄脓痰、白细胞升高等）。如果症状加重请及时复诊。',
 '[{"doc_id":"D1003","title":"抗菌药物合理使用","score":0.85,"snippet":"普通感冒避免常规使用抗生素..."}]',
 '2025-04-20 09:18:00'),

-- 会话2: 胃痛咨询
(104, '22222222-2222-2222-2222-222222222222', 2, 'user',
 '医生，我最近一周上腹隐痛，吃完饭更难受，还经常反酸打嗝，大便颜色正常。请问是什么问题？需要做胃镜吗？',
 '[{"doc_id":"D2001","title":"慢性胃炎诊治","score":0.94,"snippet":"餐后上腹痛、反酸是慢性胃炎典型表现..."}]',
 '2025-04-22 14:50:00'),

(105, '22222222-2222-2222-2222-222222222222', 2, 'assistant',
 '您的症状（餐后上腹痛、反酸、嗳气）高度提示功能性消化不良或慢性胃炎。建议先做幽门螺杆菌呼气试验，如果阳性则根除治疗。若症状反复或您想明确诊断，可以预约胃镜检查。同时注意规律饮食，避免辛辣刺激食物，可暂时服用奥美拉唑缓解症状。',
 '[{"doc_id":"D2002","title":"消化不良的鉴别诊断","score":0.91,"snippet":"胃镜是诊断胃部疾病的金标准..."}]',
 '2025-04-22 14:58:00'),

-- 会话3: 高血压用药咨询
(106, '33333333-3333-3333-3333-333333333333', 3, 'user',
 '高血压5年了，一直吃硝苯地平控释片30mg每天一次，最近量血压还是150/95，睡觉时有时心慌，该怎么办？',
 '[{"doc_id":"D3001","title":"高血压联合用药策略","score":0.96,"snippet":"单药控制不佳时，可增加普利类或沙坦类..."}]',
 '2025-04-24 10:00:00'),

(107, '33333333-3333-3333-3333-333333333333', 3, 'assistant',
 '您的血压未达标（目标<140/90），且出现心慌，建议：1) 需要调整降压方案，可联合使用血管紧张素转换酶抑制剂（如培哚普利）或血管紧张素II受体拮抗剂（如氯沙坦）；2) 检查心电图、血电解质和肾功能；3) 限盐（每日<5g）、规律运动。请尽快到心内科专科调整用药，不要自行加药。',
 '[{"doc_id":"D3002","title":"高血压合并心率增快","score":0.89,"snippet":"心率偏快可考虑添加β受体阻滞剂..."}]',
 '2025-04-24 10:12:00'),

(108, '33333333-3333-3333-3333-333333333333', 3, 'user',
 '谢谢医生，我之前没有做过心电图，会尽快去心内科。另外我平时可以喝咖啡吗？',
 NULL,
 '2025-04-24 10:25:00'),

(109, '33333333-3333-3333-3333-333333333333', 3, 'assistant',
 '咖啡因可能引起血压一过性升高和心率加快，在血压未达标阶段建议不喝或少喝。如需提神，可选低因咖啡或茶。同时监测家庭血压，记录好数值带给医生。祝您早日控制平稳！',
 '[{"doc_id":"D3003","title":"高血压患者生活方式","score":0.83,"snippet":"限制咖啡因摄入，每日咖啡不超过1杯..."}]',
 '2025-04-24 10:30:00'),

(110, '33333333-3333-3333-3333-333333333333', 3, 'user',
 '好的，我明白了，非常感谢您的详细解答！',
 NULL,
 '2025-04-24 11:45:00');


-- ============================================================================
-- 第七部分: 健康知识学习模块
-- 包含: 文章分类、文章、用户互动记录
-- ============================================================================

-- ----------------------------
-- 7.1 健康知识分类表
-- ----------------------------
DROP TABLE IF EXISTS biz_learning_category;
CREATE TABLE biz_learning_category (
    category_id    BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    parent_id      BIGINT(20)   DEFAULT 0               COMMENT '父分类ID(0=顶级)',
    category_name  VARCHAR(100) NOT NULL                COMMENT '分类名称',
    sort_order     INT(4)       DEFAULT 0               COMMENT '显示顺序',
    icon           VARCHAR(100) DEFAULT ''              COMMENT '分类图标',
    status         CHAR(1)      DEFAULT '0'             COMMENT '状态(0正常 1停用)',
    create_by      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time    DATETIME                             COMMENT '创建时间',
    update_by      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time    DATETIME                             COMMENT '更新时间',
    del_flag       CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
    remark         VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (category_id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='健康知识分类表';

-- ----------------------------
-- 7.2 健康知识文章表
-- ----------------------------
DROP TABLE IF EXISTS biz_learning_article;
CREATE TABLE biz_learning_article (
    article_id     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    category_id    BIGINT(20)   NOT NULL                COMMENT '分类ID',
    title          VARCHAR(200) NOT NULL                COMMENT '文章标题',
    summary        VARCHAR(500) DEFAULT ''              COMMENT '文章摘要',
    content        LONGTEXT     NOT NULL                COMMENT '文章内容(富文本)',
    cover_image    VARCHAR(500) DEFAULT ''              COMMENT '封面图片URL',
    author         VARCHAR(100) DEFAULT ''              COMMENT '作者',
    source         VARCHAR(200) DEFAULT ''              COMMENT '来源',
    tags           VARCHAR(500) DEFAULT ''              COMMENT '标签(逗号分隔)',
    view_count     INT(11)      DEFAULT 0               COMMENT '阅读次数',
    like_count     INT(11)      DEFAULT 0               COMMENT '点赞数',
    ai_summary     TEXT                                COMMENT 'AI生成摘要',
    is_top         TINYINT(1)   DEFAULT 0               COMMENT '是否置顶(0否 1是)',
    is_featured    TINYINT(1)   DEFAULT 0               COMMENT '是否推荐(0否 1是)',
    status         CHAR(1)      DEFAULT '0'             COMMENT '状态(0草稿 1已发布 2已下架)',
    publish_time   DATETIME                             COMMENT '发布时间',
    create_by      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time    DATETIME                             COMMENT '创建时间',
    update_by      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time    DATETIME                             COMMENT '更新时间',
    del_flag       CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
    remark         VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (article_id),
    KEY idx_category_id   (category_id),
    KEY idx_status        (status),
    KEY idx_publish_time  (publish_time)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='健康知识文章表';

-- ----------------------------
-- 7.3 用户文章互动记录表
-- ----------------------------
DROP TABLE IF EXISTS biz_learning_user_article;
CREATE TABLE biz_learning_user_article (
    id             BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT(20)   NOT NULL                COMMENT '用户ID',
    article_id     BIGINT(20)   NOT NULL                COMMENT '文章ID',
    is_bookmarked  TINYINT(1)   DEFAULT 0               COMMENT '是否收藏(0否 1是)',
    is_liked       TINYINT(1)   DEFAULT 0               COMMENT '是否点赞(0否 1是)',
    read_progress  INT(3)       DEFAULT 0               COMMENT '阅读进度(0-100)',
    read_duration  INT(11)      DEFAULT 0               COMMENT '累计阅读时长(秒)',
    last_read_time DATETIME                             COMMENT '最后阅读时间',
    quiz_score     INT(3)       DEFAULT NULL            COMMENT '测验得分(0-100)',
    create_time    DATETIME                             COMMENT '创建时间',
    update_time    DATETIME                             COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_article (user_id, article_id),
    KEY idx_user_id      (user_id),
    KEY idx_article_id   (article_id),
    KEY idx_bookmarked   (user_id, is_bookmarked)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='用户文章互动记录表';

-- ----------------------------
-- 7.4 健康知识学习菜单权限
-- ----------------------------
-- 健康知识学习目录
INSERT INTO sys_menu VALUES('406', '健康知识学习', '4', '6', 'learning', NULL, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', SYSDATE(), '', NULL, '健康知识学习目录');

-- 文章管理页面
INSERT INTO sys_menu VALUES('407', '文章管理', '406', '1', 'article', 'biz/learning/article/index', '', '', 1, 0, 'C', '0', '0', 'biz:learning:article:list', 'form', 'admin', SYSDATE(), '', NULL, '文章管理菜单');
INSERT INTO sys_menu VALUES('4030', '文章查询', '407', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:article:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4031', '文章新增', '407', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:article:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4032', '文章修改', '407', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:article:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4033', '文章删除', '407', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:article:remove', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4034', '文章导出', '407', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:article:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- 分类管理页面
INSERT INTO sys_menu VALUES('408', '分类管理', '406', '2', 'category', 'biz/learning/category/index', '', '', 1, 0, 'C', '0', '0', 'biz:learning:category:list', 'tree-table', 'admin', SYSDATE(), '', NULL, '分类管理菜单');
INSERT INTO sys_menu VALUES('4035', '分类查询', '408', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:category:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4036', '分类新增', '408', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:category:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4037', '分类修改', '408', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:category:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4038', '分类删除', '408', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:learning:category:remove', '#', 'admin', SYSDATE(), '', NULL, '');

-- ----------------------------
-- 7.5 健康知识学习角色权限分配
-- ----------------------------
-- 管理员(role_id=1): 完整权限
INSERT IGNORE INTO sys_role_menu VALUES('1', '406');
INSERT IGNORE INTO sys_role_menu VALUES('1', '407');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4030'); INSERT IGNORE INTO sys_role_menu VALUES('1', '4031');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4032'); INSERT IGNORE INTO sys_role_menu VALUES('1', '4033');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4034');
INSERT IGNORE INTO sys_role_menu VALUES('1', '408');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4035'); INSERT IGNORE INTO sys_role_menu VALUES('1', '4036');
INSERT IGNORE INTO sys_role_menu VALUES('1', '4037'); INSERT IGNORE INTO sys_role_menu VALUES('1', '4038');

-- 医疗专家(role_id=2): 完整权限
INSERT IGNORE INTO sys_role_menu VALUES('2', '406');
INSERT IGNORE INTO sys_role_menu VALUES('2', '407');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4030'); INSERT IGNORE INTO sys_role_menu VALUES('2', '4031');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4032'); INSERT IGNORE INTO sys_role_menu VALUES('2', '4033');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4034');
INSERT IGNORE INTO sys_role_menu VALUES('2', '408');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4035'); INSERT IGNORE INTO sys_role_menu VALUES('2', '4036');
INSERT IGNORE INTO sys_role_menu VALUES('2', '4037'); INSERT IGNORE INTO sys_role_menu VALUES('2', '4038');

-- 普通医生(role_id=3): 文章查询权限
INSERT IGNORE INTO sys_role_menu VALUES('3', '406');
INSERT IGNORE INTO sys_role_menu VALUES('3', '407');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4030');

-- ----------------------------
-- 7.6 健康知识分类初始数据
-- ----------------------------
INSERT INTO biz_learning_category (category_id, parent_id, category_name, sort_order, icon, status, create_by, create_time) VALUES
(100, 0, '常见疾病', 1, 'FirstAidKit', '0', 'admin', SYSDATE()),
(101, 0, '健康生活', 2, 'Apple', '0', 'admin', SYSDATE()),
(102, 0, '心理健康', 3, 'ChatDotRound', '0', 'admin', SYSDATE()),
(103, 0, '母婴健康', 4, 'User', '0', 'admin', SYSDATE()),
(104, 0, '中医养生', 5, 'MagicStick', '0', 'admin', SYSDATE()),
(105, 100, '呼吸系统', 1, '', '0', 'admin', SYSDATE()),
(106, 100, '心血管系统', 2, '', '0', 'admin', SYSDATE()),
(107, 100, '消化系统', 3, '', '0', 'admin', SYSDATE()),
(108, 101, '饮食营养', 1, '', '0', 'admin', SYSDATE()),
(109, 101, '运动健身', 2, '', '0', 'admin', SYSDATE()),
(110, 101, '睡眠健康', 3, '', '0', 'admin', SYSDATE());


-- ----------------------------
-- 7.7 健康知识文章测试数据
-- ----------------------------
INSERT INTO biz_learning_article (article_id, category_id, title, summary, content, cover_image, author, source, tags, view_count, like_count, ai_summary, is_top, is_featured, status, publish_time, create_by, create_time, update_by, update_time, del_flag) VALUES

-- === 呼吸系统 ===
(100, 105, '普通感冒的自我护理指南',
 '了解感冒的症状、居家护理方法和就医时机，帮助您科学应对常见感冒。',
 '<h2>什么是普通感冒？</h2>
<p>普通感冒（Common Cold）是由病毒引起的上呼吸道感染，是最常见的急性呼吸道疾病。成人每年平均感冒2-4次，儿童可达6-8次。</p>
<h2>常见症状</h2>
<ul>
<li><strong>鼻部症状</strong>：鼻塞、流涕、打喷嚏</li>
<li><strong>咽喉症状</strong>：咽痛、咽干、轻度咳嗽</li>
<li><strong>全身症状</strong>：低热、乏力、肌肉酸痛（较轻）</li>
</ul>
<h2>居家护理要点</h2>
<ol>
<li><strong>充分休息</strong>：保证每天7-8小时睡眠，避免过度劳累</li>
<li><strong>多饮温水</strong>：每天饮水1500-2000ml，有助于稀释痰液、缓解咽痛</li>
<li><strong>对症用药</strong>：可选用复方感冒药缓解症状，如含对乙酰氨基酚、氯苯那敏等成分的药物</li>
<li><strong>盐水漱口</strong>：用温盐水漱口可缓解咽痛，每次半茶匙盐溶于250ml温水</li>
<li><strong>保持湿度</strong>：室内湿度保持在50-60%，可用加湿器</li>
</ol>
<h2>何时需要就医？</h2>
<p>出现以下情况建议及时就医：</p>
<ul>
<li>体温持续超过38.5°C超过3天</li>
<li>症状持续超过10天无改善</li>
<li>出现呼吸困难、胸痛</li>
<li>咳黄脓痰或痰中带血</li>
</ul>
<h2>预防措施</h2>
<p>勤洗手、避免触摸面部、保持社交距离、均衡饮食、适量运动是预防感冒的关键。</p>',
 '', '张教授', '安心诊疗健康科普', '感冒,上呼吸道感染,居家护理', 156, 23, '普通感冒由病毒引起，症状包括鼻塞流涕、咽痛咳嗽。居家护理要点：充分休息、多饮温水、对症用药、盐水漱口。体温持续超38.5度或症状超10天需就医。', 1, 1, 1, '2025-03-15 09:00:00', 'admin', '2025-03-14 10:00:00', 'admin', '2025-04-20 14:30:00', '0'),

(101, 105, '支气管哮喘的日常管理',
 '哮喘患者如何通过日常管理减少发作，提高生活质量。',
 '<h2>认识哮喘</h2>
<p>支气管哮喘（Asthma）是一种慢性气道炎症性疾病，表现为反复发作的喘息、气急、胸闷或咳嗽，常在夜间及凌晨加重。</p>
<h2>哮喘的诱因</h2>
<ul>
<li>过敏原：尘螨、花粉、动物皮屑、霉菌</li>
<li>呼吸道感染：感冒、流感</li>
<li>冷空气、剧烈运动</li>
<li>烟雾、刺激性气味</li>
<li>情绪波动、精神压力</li>
</ul>
<h2>日常管理五要素</h2>
<ol>
<li><strong>规律用药</strong>：遵医嘱使用控制药物（如吸入性糖皮质激素），不可擅自停药</li>
<li><strong>正确吸入</strong>：掌握吸入装置的正确使用方法，用后漱口</li>
<li><strong>环境控制</strong>：定期清洗床品、使用防螨枕套、保持室内通风</li>
<li><strong>峰流速监测</strong>：每日记录峰流速值，及时发现病情变化</li>
<li><strong>制定行动计划</strong>：与医生共同制定哮喘发作时的应对方案</li>
</ol>
<h2>运动与哮喘</h2>
<p>哮喘患者可以运动！推荐游泳、散步、瑜伽等。运动前热身10-15分钟，必要时预防性使用支气管扩张剂。</p>',
 '', '李主任', '安心诊疗健康科普', '哮喘,慢性病管理,吸入治疗', 98, 15, '哮喘是慢性气道炎症，日常管理五要素：规律用药、正确吸入、环境控制、峰流速监测、制定行动计划。患者可适当运动，推荐游泳散步等。', 0, 1, 1, '2025-03-20 10:00:00', 'admin', '2025-03-19 09:00:00', 'admin', '2025-04-18 11:00:00', '0'),

-- === 心血管系统 ===
(102, 106, '高血压患者的饮食指南',
 '科学饮食是控制血压的基础，了解哪些食物该吃、哪些该避免。',
 '<h2>高血压与饮食的关系</h2>
<p>研究表明，合理的饮食可使收缩压降低8-14mmHg。饮食管理是高血压治疗的基石。</p>
<h2>DASH饮食原则</h2>
<p>DASH（Dietary Approaches to Stop Hypertension）饮食被证实有效降低血压：</p>
<ul>
<li><strong>多吃</strong>：全谷物、蔬菜、水果、低脂乳制品、鱼类、坚果</li>
<li><strong>少吃</strong>：红肉、甜食、含糖饮料</li>
<li><strong>限制</strong>：钠盐每日不超过5g（约一啤酒瓶盖）</li>
</ul>
<h2>降压食物推荐</h2>
<ol>
<li><strong>芹菜</strong>：含芹菜素，有辅助降压作用</li>
<li><strong>香蕉</strong>：富含钾，有助于平衡钠的影响</li>
<li><strong>深海鱼</strong>：三文鱼、鲭鱼富含Omega-3脂肪酸</li>
<li><strong>黑木耳</strong>：有助于降低血液黏稠度</li>
<li><strong>低脂牛奶</strong>：补钙有助于血压控制</li>
</ol>
<h2>饮食禁忌</h2>
<ul>
<li>腌制食品：咸菜、腊肉、酱菜</li>
<li>高钠调味品：酱油、味精、豆瓣酱需限量</li>
<li>酒精：男性每日酒精摄入不超过25g</li>
<li>浓茶和咖啡：可短期升高血压</li>
</ul>',
 '', '王博士', '安心诊疗健康科普', '高血压,DASH饮食,低盐饮食', 234, 45, 'DASH饮食可有效降低血压：多吃全谷物蔬果、低脂乳制品，限盐每日不超过5克。推荐芹菜、香蕉、深海鱼等降压食物。避免腌制食品和过量饮酒。', 1, 1, 1, '2025-02-10 08:00:00', 'admin', '2025-02-09 14:00:00', 'admin', '2025-04-22 09:00:00', '0'),

(103, 106, '冠心病的早期信号与预防',
 '识别冠心病的早期预警信号，了解预防措施，守护心脏健康。',
 '<h2>什么是冠心病？</h2>
<p>冠心病全称冠状动脉粥样硬化性心脏病，是由于冠状动脉血管发生动脉粥样硬化病变，导致血管腔狭窄或阻塞，造成心肌缺血、缺氧或坏死。</p>
<h2>早期预警信号</h2>
<ul>
<li><strong>胸闷胸痛</strong>：活动或情绪激动时出现胸骨后压榨感，休息或含服硝酸甘油后缓解</li>
<li><strong>心悸气短</strong>：轻微活动即感心跳加快、呼吸困难</li>
<li><strong>不明原因疲劳</strong>：持续疲劳感，休息后不能缓解</li>
<li><strong>左臂/下颌放射痛</strong>：疼痛向左肩、左臂内侧或下颌放射</li>
<li><strong>夜间憋醒</strong>：睡眠中突然感到胸闷、憋气</li>
</ul>
<h2>预防措施</h2>
<ol>
<li><strong>控制三高</strong>：血压&lt;140/90mmHg，空腹血糖&lt;7.0mmol/L，LDL-C&lt;2.6mmol/L</li>
<li><strong>戒烟限酒</strong>：吸烟使冠心病风险增加2-4倍</li>
<li><strong>规律运动</strong>：每周150分钟中等强度有氧运动</li>
<li><strong>健康饮食</strong>：低盐低脂、多蔬果、适量坚果</li>
<li><strong>定期体检</strong>：40岁以上每年做心电图、血脂检查</li>
</ol>
<h2>急救知识</h2>
<p>如出现持续胸痛超过15分钟不缓解，应立即拨打120，舌下含服硝酸甘油（0.5mg），嚼服阿司匹林（300mg），保持镇静等待救援。</p>',
 '', '张教授', '安心诊疗健康科普', '冠心病,胸痛,心脏健康', 187, 32, '冠心病早期信号：活动时胸闷胸痛、心悸气短、左臂放射痛、夜间憋醒。预防关键：控制三高、戒烟限酒、规律运动、定期体检。持续胸痛超15分钟应立即拨打120。', 0, 0, 1, '2025-03-01 09:00:00', 'admin', '2025-02-28 10:00:00', 'admin', '2025-04-15 16:00:00', '0'),

-- === 消化系统 ===
(104, 107, '慢性胃炎的饮食调理',
 '慢性胃炎患者如何通过饮食调理减轻症状、促进胃黏膜修复。',
 '<h2>慢性胃炎概述</h2>
<p>慢性胃炎是胃黏膜的慢性炎症，发病率高达60%以上。常见症状包括上腹隐痛、餐后饱胀、反酸嗳气等。</p>
<h2>饮食原则</h2>
<ul>
<li><strong>定时定量</strong>：三餐规律，每餐七分饱</li>
<li><strong>细嚼慢咽</strong>：每口食物咀嚼20-30次</li>
<li><strong>温度适宜</strong>：食物温度38-42°C为宜</li>
<li><strong>少食多餐</strong>：症状明显时可每日5-6餐</li>
</ul>
<h2>推荐食物</h2>
<ol>
<li><strong>小米粥</strong>：养胃易消化，可修复胃黏膜</li>
<li><strong>山药</strong>：健脾益胃，可蒸食或煮粥</li>
<li><strong>南瓜</strong>：保护胃黏膜，富含果胶</li>
<li><strong>苏打饼干</strong>：中和胃酸，缓解烧心</li>
<li><strong>蜂蜜水</strong>：饭前1.5小时饮用可抑制胃酸分泌</li>
</ol>
<h2>禁忌食物</h2>
<ul>
<li>辛辣刺激：辣椒、花椒、芥末</li>
<li>酸性食物：柠檬、醋、未熟水果</li>
<li>浓茶咖啡：刺激胃酸分泌</li>
<li>油炸食品：加重胃负担</li>
<li>生冷食物：冰饮、凉菜</li>
</ul>',
 '', '陈医生', '安心诊疗健康科普', '慢性胃炎,饮食调理,养胃', 145, 28, '慢性胃炎饮食原则：定时定量、细嚼慢咽、温度适宜、少食多餐。推荐小米粥、山药、南瓜等养胃食物。忌辛辣、酸性、油炸、生冷食物。', 0, 1, 1, '2025-03-25 10:00:00', 'admin', '2025-03-24 09:00:00', 'admin', '2025-04-19 10:00:00', '0'),

(105, 107, '幽门螺杆菌感染：你需要知道的一切',
 '幽门螺杆菌是胃炎、消化性溃疡的主要致病因素，了解检测和治疗方法。',
 '<h2>什么是幽门螺杆菌？</h2>
<p>幽门螺杆菌（Helicobacter pylori，Hp）是一种定植于胃黏膜的革兰阴性杆菌，我国感染率约50%。</p>
<h2>传播途径</h2>
<ul>
<li><strong>口-口传播</strong>：共用餐具、亲吻、口对口喂食</li>
<li><strong>粪-口传播</strong>：污染的水源和食物</li>
<li><strong>家庭聚集性</strong>：家庭成员间相互传播</li>
</ul>
<h2>检测方法</h2>
<ol>
<li><strong>C13/C14呼气试验</strong>：最常用，无创、准确率高（推荐）</li>
<li><strong>血清抗体检测</strong>：仅反映是否感染过，不能判断现症感染</li>
<li><strong>粪便抗原检测</strong>：适合儿童和老人</li>
<li><strong>胃镜活检</strong>：可同时评估胃黏膜情况</li>
</ol>
<h2>根除治疗</h2>
<p>标准四联疗法：质子泵抑制剂 + 铋剂 + 两种抗生素，疗程14天。治疗结束后4周复查呼气试验。</p>
<h2>预防再感染</h2>
<ul>
<li>实行分餐制或使用公筷</li>
<li>餐具定期消毒</li>
<li>避免口对口喂食幼儿</li>
<li>家庭成员同时检测和治疗</li>
</ul>',
 '', '刘医师', '安心诊疗健康科普', '幽门螺杆菌,胃炎,呼气试验', 203, 38, '幽门螺杆菌我国感染率约50%，经口-口和粪-口传播。推荐C13呼气试验检测。标准四联疗法根除，疗程14天。预防关键：分餐制、公筷、餐具消毒。', 1, 0, 1, '2025-04-01 09:00:00', 'admin', '2025-03-31 14:00:00', 'admin', '2025-04-25 08:00:00', '0'),

-- === 饮食营养 ===
(106, 108, '中国居民膳食指南核心要点',
 '2022版中国居民膳食指南的八大核心准则，科学指导日常饮食。',
 '<h2>准则一：食物多样，合理搭配</h2>
<p>每天摄入12种以上食物，每周25种以上。谷类为主，每天谷薯类200-300g。</p>
<h2>准则二：吃动平衡，健康体重</h2>
<p>每周至少5天中等强度运动，每次30分钟以上。维持BMI在18.5-23.9之间。</p>
<h2>准则三：多吃蔬果、奶类、全谷</h2>
<ul>
<li>蔬菜每天300-500g，深色蔬菜占一半</li>
<li>水果每天200-350g</li>
<li>奶制品每天300-500ml</li>
<li>全谷物占主食的1/3</li>
</ul>
<h2>准则四：适量吃鱼、禽、蛋、瘦肉</h2>
<p>每周吃鱼2次，每天1个鸡蛋，少吃肥肉和加工肉制品。</p>
<h2>准则五：少盐少油，控糖限酒</h2>
<ul>
<li>盐：每天不超过5g</li>
<li>油：每天25-30g</li>
<li>添加糖：每天不超过25g</li>
<li>酒精：成年男性每天不超过15g纯酒精</li>
</ul>
<h2>准则六至八</h2>
<p>规律进餐、足量饮水（每天1500-1700ml）、会烹会选、公筷分餐。</p>',
 '', '营养科', '中国营养学会', '膳食指南,营养,均衡饮食', 312, 56, '2022版膳食指南八大准则：食物多样每天12种以上、吃动平衡、多吃蔬果奶类全谷、适量鱼禽蛋肉、少盐少油控糖限酒、规律进餐、足量饮水、公筷分餐。', 1, 1, 1, '2025-01-15 08:00:00', 'admin', '2025-01-14 10:00:00', 'admin', '2025-04-21 15:00:00', '0'),

(107, 108, '维生素D缺乏：现代人的隐形健康危机',
 '维生素D缺乏影响骨骼、免疫和情绪，了解如何科学补充。',
 '<h2>维生素D的重要性</h2>
<p>维生素D不仅促进钙吸收，还参与免疫调节、情绪调控、心血管保护等多种生理功能。</p>
<h2>缺乏的信号</h2>
<ul>
<li>经常感到疲劳、乏力</li>
<li>骨痛、关节疼痛</li>
<li>情绪低落、容易焦虑</li>
<li>免疫力下降，反复感冒</li>
<li>伤口愈合缓慢</li>
</ul>
<h2>高危人群</h2>
<ol>
<li>长期室内工作者</li>
<li>老年人（皮肤合成能力下降）</li>
<li>肥胖人群（维生素D储存在脂肪中）</li>
<li>深肤色人群</li>
<li>孕期和哺乳期女性</li>
</ol>
<h2>科学补充方法</h2>
<ul>
<li><strong>日晒</strong>：每天10-30分钟，露出手臂和面部</li>
<li><strong>食物</strong>：三文鱼、蛋黄、强化牛奶、蘑菇</li>
<li><strong>补充剂</strong>：成人每天400-800IU，缺乏者可遵医嘱增加</li>
</ul>
<h2>检测指标</h2>
<p>血清25(OH)D水平：&lt;20ng/mL为缺乏，20-30ng/mL为不足，30-100ng/mL为充足。</p>',
 '', '赵医生', '安心诊疗健康科普', '维生素D,骨骼健康,免疫力', 128, 19, '维生素D缺乏影响骨骼免疫和情绪。高危人群：室内工作者、老年人、肥胖者。补充方法：日晒10-30分钟、食用三文鱼蛋黄、必要时补充剂400-800IU/天。', 0, 0, 1, '2025-04-05 10:00:00', 'admin', '2025-04-04 09:00:00', 'admin', '2025-04-23 11:00:00', '0'),

-- === 运动健身 ===
(108, 109, '办公室久坐族的5分钟拉伸操',
 '简单有效的办公室拉伸动作，缓解颈椎腰椎不适，提升工作效率。',
 '<h2>久坐的危害</h2>
<p>连续久坐超过1小时，代谢减缓、血液循环变差，长期可导致颈椎病、腰椎间盘突出、肥胖等问题。</p>
<h2>5分钟拉伸方案</h2>
<h3>第1分钟：颈部拉伸</h3>
<ul>
<li>头部缓慢前屈后伸，各保持5秒</li>
<li>左右侧屈，耳朵尽量靠近肩膀，各保持5秒</li>
<li>左右转头，各保持5秒</li>
</ul>
<h3>第2分钟：肩部放松</h3>
<ul>
<li>双肩向上耸起，保持3秒后放下，重复5次</li>
<li>双肩向后画圈，幅度由小到大，重复10次</li>
<li>双手交叉抱头，双肘向后打开，保持10秒</li>
</ul>
<h3>第3分钟：腰背伸展</h3>
<ul>
<li>坐直，双手交叉向上伸展，保持10秒</li>
<li>坐位体前屈，双手尽量触碰脚尖，保持10秒</li>
<li>坐位扭转，左右各保持10秒</li>
</ul>
<h3>第4分钟：手腕手指</h3>
<ul>
<li>手腕顺时针、逆时针各转10圈</li>
<li>用力握拳5秒，然后张开手指5秒，重复5次</li>
</ul>
<h3>第5分钟：腿部活动</h3>
<ul>
<li>坐位伸直一条腿，保持5秒，交替进行</li>
<li>踮脚尖10次，促进小腿血液循环</li>
</ul>
<p><strong>建议</strong>：每坐1小时起身活动5分钟，配合以上拉伸操效果更佳。</p>',
 '', '运动医学科', '安心诊疗健康科普', '办公室运动,拉伸,颈椎腰椎', 267, 42, '久坐超1小时代谢减缓。5分钟拉伸操：颈部前后左右拉伸、肩部耸肩画圈、腰背伸展扭转、手腕握拳伸展、腿部伸直踮脚。建议每坐1小时活动5分钟。', 0, 1, 1, '2025-03-10 10:00:00', 'admin', '2025-03-09 14:00:00', 'admin', '2025-04-17 09:00:00', '0'),

-- === 睡眠健康 ===
(109, 110, '改善失眠的10个科学方法',
 '不用药物也能改善睡眠，这些经过科学验证的方法值得一试。',
 '<h2>失眠的定义</h2>
<p>入睡困难（超过30分钟）、频繁觉醒、早醒，每周至少3次，持续3个月以上，称为慢性失眠。</p>
<h2>10个科学方法</h2>
<ol>
<li><strong>固定作息</strong>：每天同一时间上床和起床，包括周末</li>
<li><strong>睡前仪式</strong>：建立30分钟的放松流程，如阅读、泡脚、听轻音乐</li>
<li><strong>控制光线</strong>：睡前1小时减少蓝光暴露，使用暖色灯光</li>
<li><strong>卧室环境</strong>：温度18-22°C，保持安静黑暗</li>
<li><strong>限制咖啡因</strong>：下午2点后避免咖啡、浓茶</li>
<li><strong>规律运动</strong>：每天30分钟有氧运动，但睡前3小时避免剧烈运动</li>
<li><strong>控制午睡</strong>：午睡不超过30分钟，下午3点后不午睡</li>
<li><strong>床只用于睡觉</strong>：不在床上看手机、看电视、工作</li>
<li><strong>放松训练</strong>：渐进性肌肉放松、腹式呼吸、正念冥想</li>
<li><strong>记录睡眠日记</strong>：记录入睡时间、觉醒次数，帮助识别问题</li>
</ol>
<h2>何时就医？</h2>
<p>自我调节2-4周无效，或失眠严重影响日间工作生活，建议到睡眠医学科就诊。认知行为治疗（CBT-I）是一线推荐疗法。</p>',
 '', '吴护士', '安心诊疗健康科普', '失眠,睡眠,认知行为治疗', 198, 35, '慢性失眠需每周3次持续3月以上。10个科学方法：固定作息、睡前仪式、控制光线、卧室环境18-22度、限咖啡因、规律运动、限午睡、床只睡觉、放松训练、睡眠日记。CBT-I是一线疗法。', 0, 0, 1, '2025-03-28 09:00:00', 'admin', '2025-03-27 10:00:00', 'admin', '2025-04-20 16:00:00', '0'),

-- === 心理健康 ===
(110, 102, '焦虑情绪的自我调节技巧',
 '学习识别焦虑信号，掌握实用的自我调节方法，找回内心平静。',
 '<h2>认识焦虑</h2>
<p>适度的焦虑是正常的保护机制，但当焦虑持续存在、影响日常生活时，就需要学会调节。</p>
<h2>焦虑的身体信号</h2>
<ul>
<li>心跳加速、胸闷</li>
<li>肌肉紧张、肩颈僵硬</li>
<li>手心出汗、口干</li>
<li>胃部不适、食欲改变</li>
<li>睡眠困难、注意力不集中</li>
</ul>
<h2>即时缓解技巧</h2>
<ol>
<li><strong>4-7-8呼吸法</strong>：吸气4秒→屏息7秒→呼气8秒，重复3-4次</li>
<li><strong>5-4-3-2-1感官法</strong>：说出5个看到的、4个触摸到的、3个听到的、2个闻到的、1个尝到的</li>
<li><strong>渐进性肌肉放松</strong>：从脚趾到头部，依次紧绷5秒再放松</li>
<li><strong>接地练习</strong>：双脚踩地，感受地面的支撑</li>
</ol>
<h2>长期调节策略</h2>
<ul>
<li><strong>规律运动</strong>：每周3-5次有氧运动，可显著降低焦虑水平</li>
<li><strong>正念冥想</strong>：每天10-15分钟，专注当下</li>
<li><strong>限制信息过载</strong>：减少刷手机和负面新闻的时间</li>
<li><strong>社交支持</strong>：与信任的人倾诉，参加社交活动</li>
<li><strong>写日记</strong>：记录焦虑触发事件和感受，帮助识别模式</li>
</ul>
<h2>何时寻求专业帮助？</h2>
<p>焦虑持续超过2周、出现惊恐发作、回避社交场合、影响工作学习时，建议咨询心理科或精神科医生。</p>',
 '', '心理咨询师', '安心诊疗健康科普', '焦虑,心理健康,呼吸放松', 176, 29, '焦虑自我调节：即时技巧包括4-7-8呼吸法、5-4-3-2-1感官法、渐进性肌肉放松。长期策略：规律运动、正念冥想、限制信息、社交支持。持续超2周应寻求专业帮助。', 0, 1, 1, '2025-04-10 09:00:00', 'admin', '2025-04-09 10:00:00', 'admin', '2025-04-24 14:00:00', '0'),

-- === 中医养生 ===
(111, 104, '四季养生：顺应自然的健康智慧',
 '中医强调天人合一，四季养生各有侧重，了解不同季节的养生要点。',
 '<h2>春季养生（立春至立夏）</h2>
<ul>
<li><strong>养肝为主</strong>：春属木，对应肝脏</li>
<li><strong>饮食</strong>：多食绿色蔬菜，如菠菜、芹菜；少酸多甘</li>
<li><strong>起居</strong>：早睡早起，舒展身体</li>
<li><strong>情志</strong>：保持心情舒畅，避免抑郁</li>
</ul>
<h2>夏季养生（立夏至立秋）</h2>
<ul>
<li><strong>养心为主</strong>：夏属火，对应心脏</li>
<li><strong>饮食</strong>：清淡为主，多食苦瓜、绿豆、莲子</li>
<li><strong>起居</strong>：晚睡早起，午休30分钟</li>
<li><strong>防暑</strong>：避免正午外出，适当出汗排毒</li>
</ul>
<h2>秋季养生（立秋至立冬）</h2>
<ul>
<li><strong>养肺为主</strong>：秋属金，对应肺脏</li>
<li><strong>饮食</strong>：滋阴润燥，多食梨、银耳、百合</li>
<li><strong>起居</strong>：早睡早起，注意保暖</li>
<li><strong>情志</strong>：避免悲伤，保持乐观</li>
</ul>
<h2>冬季养生（立冬至立春）</h2>
<ul>
<li><strong>养肾为主</strong>：冬属水，对应肾脏</li>
<li><strong>饮食</strong>：温补为主，可食羊肉、核桃、黑芝麻</li>
<li><strong>起居</strong>：早睡晚起，注意保暖</li>
<li><strong>运动</strong>：选择温和运动，如太极、八段锦</li>
</ul>',
 '', '中医科', '安心诊疗健康科普', '四季养生,中医,天人合一', 143, 22, '四季养生要点：春季养肝多食绿色蔬菜早睡早起；夏季养心清淡饮食午休30分钟；秋季养肺滋阴润燥多食梨银耳；冬季养肾温补早睡晚起。', 0, 0, 1, '2025-04-15 08:00:00', 'admin', '2025-04-14 09:00:00', 'admin', '2025-04-25 10:00:00', '0'),

-- === 草稿和下架文章 ===
(112, 106, '心律失常的识别与应对（草稿）',
 '不同类型心律失常的症状识别和紧急处理方法。',
 '<h2>正在撰写中...</h2><p>本文将介绍常见心律失常类型（房颤、室早、窦性心动过速等）的症状、识别方法和应对措施。</p>',
 '', '张教授', '', '心律失常,房颤', 12, 0, NULL, 0, 0, 0, NULL, 'admin', '2025-04-20 10:00:00', 'admin', '2025-04-20 10:00:00', '0'),

(113, 103, '新生儿黄疸的家长指南',
 '了解新生儿黄疸的原因、分类和家庭护理注意事项。',
 '<h2>什么是新生儿黄疸？</h2>
<p>新生儿黄疸是由于血液中胆红素水平升高，导致皮肤和眼白发黄的现象。约60%的足月儿和80%的早产儿会出现黄疸。</p>
<h2>生理性黄疸 vs 病理性黄疸</h2>
<ul>
<li><strong>生理性黄疸</strong>：出生后2-3天出现，4-5天达高峰，7-10天消退。一般情况良好。</li>
<li><strong>病理性黄疸</strong>：出生24小时内出现，或持续超过2周，需及时就医。</li>
</ul>
<h2>家庭护理</h2>
<ol>
<li>充分喂养，促进胆红素排泄</li>
<li>观察黄疸变化：在自然光下观察皮肤颜色</li>
<li>注意精神状态和吃奶情况</li>
<li>遵医嘱进行蓝光治疗</li>
</ol>
<h2>何时紧急就医？</h2>
<ul>
<li>黄疸迅速加重或扩展到四肢</li>
<li>宝宝精神萎靡、拒奶</li>
<li>大便颜色发白或灰白</li>
</ul>',
 '', '儿科', '安心诊疗健康科普', '新生儿黄疸,母婴,蓝光治疗', 67, 8, '新生儿黄疸约60%足月儿出现。生理性黄疸2-3天出现7-10天消退。家庭护理：充分喂养、观察黄疸变化。病理性黄疸或黄疸迅速加重需紧急就医。', 0, 0, 2, '2025-03-05 09:00:00', 'admin', '2025-03-04 10:00:00', 'admin', '2025-04-10 11:00:00', '0');


-- ----------------------------
-- 7.8 用户文章互动记录测试数据
-- ----------------------------
INSERT INTO biz_learning_user_article (id, user_id, article_id, is_bookmarked, is_liked, read_progress, read_duration, last_read_time, create_time, update_time) VALUES
-- 患者 黄小明(user_id=40) 的互动
(100, 40, 100, 1, 1, 100, 320, '2025-04-20 10:15:00', '2025-04-20 10:00:00', '2025-04-20 10:15:00'),
(101, 40, 102, 1, 1, 85, 480, '2025-04-21 14:30:00', '2025-04-21 14:00:00', '2025-04-21 14:30:00'),
(102, 40, 106, 1, 0, 100, 600, '2025-04-22 09:00:00', '2025-04-22 08:30:00', '2025-04-22 09:00:00'),
(103, 40, 108, 0, 1, 60, 180, '2025-04-23 16:00:00', '2025-04-23 15:45:00', '2025-04-23 16:00:00'),
(104, 40, 110, 1, 0, 45, 150, '2025-04-24 20:00:00', '2025-04-24 19:30:00', '2025-04-24 20:00:00'),

-- 患者 徐小红(user_id=41) 的互动
(105, 41, 100, 0, 1, 100, 280, '2025-04-19 11:00:00', '2025-04-19 10:30:00', '2025-04-19 11:00:00'),
(106, 41, 104, 1, 1, 100, 520, '2025-04-20 15:00:00', '2025-04-20 14:30:00', '2025-04-20 15:00:00'),
(107, 41, 105, 1, 0, 75, 350, '2025-04-22 10:00:00', '2025-04-22 09:30:00', '2025-04-22 10:00:00'),
(108, 41, 109, 0, 1, 100, 420, '2025-04-23 22:00:00', '2025-04-23 21:30:00', '2025-04-23 22:00:00'),

-- 患者 高大伟(user_id=42) 的互动
(109, 42, 101, 1, 1, 100, 380, '2025-04-18 09:00:00', '2025-04-18 08:30:00', '2025-04-18 09:00:00'),
(110, 42, 102, 1, 1, 100, 550, '2025-04-19 14:00:00', '2025-04-19 13:00:00', '2025-04-19 14:00:00'),
(111, 42, 103, 1, 0, 90, 420, '2025-04-21 16:00:00', '2025-04-21 15:30:00', '2025-04-21 16:00:00'),
(112, 42, 108, 0, 1, 100, 300, '2025-04-23 08:00:00', '2025-04-23 07:30:00', '2025-04-23 08:00:00'),

-- 患者 林小芳(user_id=43) 的互动
(113, 43, 106, 1, 1, 100, 720, '2025-04-20 11:00:00', '2025-04-20 10:00:00', '2025-04-20 11:00:00'),
(114, 43, 107, 1, 0, 100, 380, '2025-04-21 09:00:00', '2025-04-21 08:30:00', '2025-04-21 09:00:00'),
(115, 43, 110, 0, 1, 80, 280, '2025-04-24 21:00:00', '2025-04-24 20:30:00', '2025-04-24 21:00:00'),
(116, 43, 111, 1, 1, 100, 350, '2025-04-25 10:00:00', '2025-04-25 09:30:00', '2025-04-25 10:00:00'),

-- 患者 何建国(user_id=44) 的互动
(117, 44, 102, 1, 1, 100, 500, '2025-04-17 15:00:00', '2025-04-17 14:00:00', '2025-04-17 15:00:00'),
(118, 44, 103, 0, 1, 70, 320, '2025-04-20 10:00:00', '2025-04-20 09:30:00', '2025-04-20 10:00:00'),
(119, 44, 105, 1, 0, 100, 450, '2025-04-22 16:00:00', '2025-04-22 15:00:00', '2025-04-22 16:00:00');


-- ============================================================================
-- 附录: AI模型配置验证脚本
-- 执行以下查询验证数据库是否正确初始化
-- ============================================================================

-- 1. 检查AI模型配置表
-- SELECT 'AI模型配置' AS check_item;
-- SELECT id, model_name, provider, model_type, is_default, status FROM biz_ai_model_config ORDER BY sort_order;

-- 2. 检查默认对话模型
-- SELECT '默认对话模型' AS check_item;
-- SELECT id, model_name, api_base_url FROM biz_ai_model_config WHERE model_type = 'text' AND is_default = 1 AND status = 1;

-- 3. 检查默认Embedding模型
-- SELECT '默认Embedding模型' AS check_item;
-- SELECT id, model_name, api_base_url FROM biz_ai_model_config WHERE model_type = 'embedding' AND is_default = 1 AND status = 1;

-- 4. 检查菜单配置
-- SELECT '菜单配置' AS check_item;
-- SELECT menu_id, menu_name, parent_id, perms FROM sys_menu WHERE menu_id IN (118, 401, 402, 403, 404, 405) ORDER BY menu_id;

-- 5. 检查角色权限
-- SELECT '角色权限统计' AS check_item;
-- SELECT r.role_name, COUNT(rm.menu_id) AS menu_count
-- FROM sys_role r LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
-- GROUP BY r.role_id, r.role_name ORDER BY r.role_id;

-- 6. 用户统计
-- SELECT '用户统计' AS check_item;
-- SELECT COUNT(*) AS total_users FROM sys_user WHERE del_flag = '0';


-- ============================================================================
-- 完成！数据库初始化脚本执行完毕
-- ============================================================================
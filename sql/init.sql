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
INSERT INTO sys_role VALUES('3', '患者',       'patient',        3, 2, 1, '0', '0', 'admin', SYSDATE(), '', NULL, '患者角色，使用问诊服务');


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
INSERT INTO sys_menu VALUES('118', 'AI模型配置管理', '4', '7', 'ai-model', 'system/aiModel/index',     '', '', 1, 0, 'C', '0', '0', 'system:aiModel:list', 'cpu',        'admin', SYSDATE(), '', NULL, 'AI大模型参数管理菜单');

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

-- ---- 业务管理 - 健康档案管理 (404 + 4009-4012, 4023-4024) ----
INSERT INTO sys_menu VALUES('404',  '健康档案管理', '4', '4', 'health-profile', 'biz/profile/index', '', 'HealthProfile', 1, 0, 'C', '0', '0', 'biz:profile:list',   'profile', 'admin', SYSDATE(), '', NULL, '健康档案管理菜单');
INSERT INTO sys_menu VALUES('4009', '档案新增',     '404', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:add',    '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4010', '档案修改',     '404', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:edit',   '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4011', '档案查询',     '404', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:query',  '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4012', '档案删除',     '404', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:remove', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4023', '档案导出',     '404', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:export', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES('4024', '档案导入',     '404', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:import', '#', 'admin', SYSDATE(), '', NULL, '');


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

-- 医疗专家(role_id=2)仅拥有知识库/文档管理权限，无需系统管理菜单


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


-- 用药提醒表
CREATE TABLE IF NOT EXISTS `biz_medication` (
                                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                                `drug_name` varchar(100) NOT NULL COMMENT '药品名称',
                                                `dosage` varchar(50) DEFAULT NULL COMMENT '剂量（如：1片、5ml）',
                                                `frequency` varchar(20) NOT NULL DEFAULT 'daily' COMMENT '频次（daily=每日/twice=每日两次/thrice=每日三次/weekly=每周/asNeeded=按需）',
                                                `reminder_times` varchar(200) DEFAULT NULL COMMENT '提醒时间（JSON数组，如 ["08:00","20:00"]）',
                                                `start_date` date DEFAULT NULL COMMENT '开始日期',
                                                `end_date` date DEFAULT NULL COMMENT '结束日期（null表示长期）',
                                                `notes` varchar(500) DEFAULT NULL COMMENT '用药备注（饭前/饭后等）',
                                                `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0启用 1停用）',
                                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
                                                `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                                PRIMARY KEY (`id`),
                                                KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药提醒表';

-- 用药打卡记录表
CREATE TABLE IF NOT EXISTS `biz_medication_log` (
                                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                    `medication_id` bigint NOT NULL COMMENT '用药ID',
                                                    `user_id` bigint NOT NULL COMMENT '用户ID',
                                                    `scheduled_time` varchar(10) DEFAULT NULL COMMENT '计划时间（如 08:00）',
                                                    `taken_time` datetime DEFAULT NULL COMMENT '实际服药时间',
                                                    `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0已打卡 1漏服）',
                                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                    PRIMARY KEY (`id`),
                                                    KEY `idx_medication_id` (`medication_id`),
                                                    KEY `idx_user_date` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药打卡记录表';

-- 插入用药提醒测试数据
INSERT INTO `biz_medication` (`user_id`, `drug_name`, `dosage`, `frequency`, `reminder_times`, `start_date`, `end_date`, `notes`, `status`) VALUES
                                                                                                                                                (21, '阿莫西林胶囊', '1粒', 'twice', '["08:00", "20:00"]', '2023-10-01', '2023-10-07', '饭后服用', '0'),
                                                                                                                                                (21, '维生素C片', '2片', 'daily', '["09:00"]', '2023-10-01', NULL, '随餐服用', '0'),
                                                                                                                                                (21, '布洛芬缓释胶囊', '1粒', 'asNeeded', '[]', '2023-10-05', '2023-10-10', '疼痛时服用，间隔至少6小时', '0'),
                                                                                                                                                (21, '降压药', '1片', 'daily', '["07:00"]', '2023-01-01', NULL, '晨起空腹服用', '0');

-- 插入用药打卡记录测试数据
INSERT INTO `biz_medication_log` (`medication_id`, `user_id`, `scheduled_time`, `taken_time`, `status`) VALUES
                                                                                                            (1, 1001, '08:00', '2023-10-01 08:05:00', '0'),
                                                                                                            (1, 1001, '20:00', '2023-10-01 20:10:00', '0'),
                                                                                                            (2, 1001, '09:00', '2023-10-01 09:00:00', '0'),
                                                                                                            (1, 1001, '08:00', NULL, '1'), -- 漏服
                                                                                                            (3, 1002, NULL, '2023-10-05 14:30:00', '0'), -- 按需服用
                                                                                                            (4, 1002, '07:00', '2023-10-01 07:00:00', '0');

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
-- AI模型管理
INSERT IGNORE INTO sys_role_menu VALUES('1', '118');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1050');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1051');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1052');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1053');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1054');
INSERT IGNORE INTO sys_role_menu VALUES('1', '1055');


-- ---- 医疗专家(role_id=2): 知识库/文档管理 ----
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


-- ---- 患者(role_id=3): 智能问诊 + 个人档案 ----
-- 业务管理目录
INSERT IGNORE INTO sys_role_menu VALUES('3', '4');
-- 问诊相关权限（流式对话/历史/详情/删除/编辑/置顶/满意度）
INSERT IGNORE INTO sys_role_menu VALUES('3', '403');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4007');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4008');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4013');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4014');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4015');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4016');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4020');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4021');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4022');
-- 健康档案管理（增删改查）
INSERT IGNORE INTO sys_role_menu VALUES('3', '404');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4009');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4010');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4011');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4012');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4023');
INSERT IGNORE INTO sys_role_menu VALUES('3', '4024');


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

-- 患者 (role_id=3)
INSERT IGNORE INTO sys_user VALUES(20, 'patient_huang', '黄小明', '00', 'huang.patient@email.com', '13800004001', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者1');
INSERT IGNORE INTO sys_user VALUES(21, 'patient_xu',   '徐小红', '00', 'xu.patient@email.com',   '13800004002', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者2');
INSERT IGNORE INTO sys_user VALUES(22, 'patient_gao',  '高大伟', '00', 'gao.patient@email.com',  '13800004003', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者3');
INSERT IGNORE INTO sys_user VALUES(23, 'patient_lin',  '林小芳', '00', 'lin.patient@email.com',  '13800004004', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者4');
INSERT IGNORE INTO sys_user VALUES(24, 'patient_he',   '何建国', '00', 'he.patient@email.com',   '13800004005', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL, '测试患者5');

-- 用户角色关联
INSERT IGNORE INTO sys_user_role VALUES(10, 2);
INSERT IGNORE INTO sys_user_role VALUES(11, 2);
INSERT IGNORE INTO sys_user_role VALUES(12, 2);
INSERT IGNORE INTO sys_user_role VALUES(20, 3);
INSERT IGNORE INTO sys_user_role VALUES(21, 3);
INSERT IGNORE INTO sys_user_role VALUES(22, 3);
INSERT IGNORE INTO sys_user_role VALUES(23, 3);
INSERT IGNORE INTO sys_user_role VALUES(24, 3);


-- ---- 6.2 用户健康档案测试数据 ----
INSERT INTO biz_user_profile (user_id, birthday, gender, blood_type, height, weight, allergy_history, medical_history, family_history, lifestyle, status, create_time, update_time, del_flag, remark)
VALUES
    (1,  '1990-05-12', '0', 'A',  175.5, 72.3, '青霉素过敏',    '无',             '母亲高血压',   '偶尔吸烟，每周运动2次', '0', '2024-01-10 08:30:00', '2025-04-20 14:25:00', '0', '体检正常'),
    (10, '1988-02-29', '0', 'O',  168.0, 62.5, '无',            '慢性胃炎',       '父亲胃癌',     '喜辛辣，三餐不规律',     '0', '2024-09-19 08:00:00', '2025-04-25 09:30:00', '0', '建议胃镜检查'),
    (11, '1985-11-23', '1', 'B',  162.0, 55.8, '花粉过敏',      '甲状腺功能减退', '父亲糖尿病',   '不吸烟，规律瑜伽',       '0', '2024-02-15 10:15:00', '2025-04-22 09:10:00', '0', '需定期复查甲功'),
    (12, '2000-07-08', '0', 'O',  180.2, 80.5, '无',            '无',             '无',           '运动员，每天锻炼',       '0', '2024-03-20 12:00:00', '2025-04-18 16:45:00', '0', '身体健康'),
    (20, '1978-12-01', '1', 'AB', 160.0, 68.0, '海鲜过敏',      '高血压，高血脂', '母亲冠心病',   '不吸烟，偶尔饮酒',       '0', '2024-01-05 09:00:00', '2025-04-21 11:20:00', '0', '长期服药控制血压'),
    (21, '1995-03-19', '0', 'A',  170.0, 65.2, '无',            '哮喘',           '父亲哮喘',     '吸烟10年，不饮酒',       '0', '2024-04-10 14:30:00', '2025-04-19 13:40:00', '0', '哮喘控制良好'),
    (22, '1982-09-25', '1', 'B',  165.5, 71.0, '无',            '2型糖尿病',      '祖母糖尿病',   '规律作息，不烟酒',       '0', '2024-05-18 11:00:00', '2025-04-23 08:00:00', '0', '血糖控制中'),
    (23, '2005-04-14', '0', 'O',  172.0, 60.0, '尘螨过敏',      '无',             '无',           '学生，久坐少动',         '0', '2024-06-22 09:45:00', '2025-04-24 15:30:00', '0', '建议增加运动'),
    (24, '1970-06-30', '1', 'AB', 158.0, 52.0, '无',            '骨质疏松',       '母亲骨质疏松', '不吸烟，每日散步',       '0', '2024-07-11 13:20:00', '2025-04-20 10:10:00', '0', '补充钙剂');


-- ---- 6.3 问诊会话测试数据 ----
INSERT INTO biz_chat_session (session_id, user_id, title, symptom_summary, ai_diagnosis, recommended_label, message_count, satisfaction, tokens_used, last_message_time, status, create_time, update_time, del_flag, remark)
VALUES
    ('11111111-1111-1111-1111-111111111111', 20, '感冒咳嗽问诊',
     '患者三天前开始流涕、咽痛，今日出现干咳，无发热',
     '急性上呼吸道感染（普通感冒）可能性大', '呼吸内科',
     4, 4, 350, '2025-04-20 10:30:00', '1',
     '2025-04-20 09:00:00', '2025-04-20 10:35:00', '0', '患者对回复满意'),

    ('22222222-2222-2222-2222-222222222222', 21, '胃痛咨询',
     '上腹部隐痛一周，餐后加重，伴反酸嗳气，无黑便',
     '功能性消化不良或慢性胃炎可能，建议完善胃镜检查', '消化内科',
     2, NULL, 210, '2025-04-22 15:20:00', '0',
     '2025-04-22 14:50:00', '2025-04-22 15:25:00', '0', '会话进行中，等待用户追问'),

    ('33333333-3333-3333-3333-333333333333', 22, '高血压用药咨询',
     '高血压病史5年，目前服用硝苯地平控释片，血压控制欠佳，收缩压150左右',
     '血压未达标，建议联合使用普利类或沙坦类药物，需心内科进一步调整方案', '心血管内科',
     5, 5, 520, '2025-04-24 11:45:00', '1',
     '2025-04-24 10:00:00', '2025-04-24 11:50:00', '0', '患者评价很好');


-- ---- 6.4 对话消息测试数据 ----
INSERT INTO biz_chat_message (message_id, session_id, user_id, role, content, retrieval_results, create_time)
VALUES
-- 会话1: 感冒咳嗽问诊
(100, '11111111-1111-1111-1111-111111111111', 20, 'user',
 '医生您好，我最近三天开始流鼻涕、嗓子疼，今天开始干咳，没有发烧，请问该吃什么药？',
 '[{"doc_id":"D1001","title":"普通感冒诊疗指南","score":0.92,"snippet":"普通感冒以对症治疗为主，无发热时无需使用抗生素..."}]',
 '2025-04-20 09:00:00'),

(101, '11111111-1111-1111-1111-111111111111', 20, 'assistant',
 '根据您描述的症状（流涕、咽痛、干咳、无发热），首先考虑普通感冒。建议：多休息、多饮水；可使用含氯苯那敏的复方感冒药缓解症状；干咳明显时可用右美沙芬。如症状持续>7天或出现发热、呼吸困难，请及时就医。',
 '[{"doc_id":"D1002","title":"咳嗽的诊疗流程","score":0.88,"snippet":"干咳可选用右美沙芬，需排除哮喘等病因..."}]',
 '2025-04-20 09:05:00'),

(102, '11111111-1111-1111-1111-111111111111', 20, 'user',
 '谢谢医生，我家里有复方氨酚烷胺片，可以吃吗？另外需要吃消炎药吗？',
 NULL,
 '2025-04-20 09:12:00'),

(103, '11111111-1111-1111-1111-111111111111', 20, 'assistant',
 '复方氨酚烷胺片可以缓解感冒症状，按说明书服用即可。普通感冒多为病毒性，不建议自行服用"消炎药"（即抗生素），除非合并细菌感染证据（如黄脓痰、白细胞升高等）。如果症状加重请及时复诊。',
 '[{"doc_id":"D1003","title":"抗菌药物合理使用","score":0.85,"snippet":"普通感冒避免常规使用抗生素..."}]',
 '2025-04-20 09:18:00'),

-- 会话2: 胃痛咨询
(104, '22222222-2222-2222-2222-222222222222', 21, 'user',
 '医生，我最近一周上腹隐痛，吃完饭更难受，还经常反酸打嗝，大便颜色正常。请问是什么问题？需要做胃镜吗？',
 '[{"doc_id":"D2001","title":"慢性胃炎诊治","score":0.94,"snippet":"餐后上腹痛、反酸是慢性胃炎典型表现..."}]',
 '2025-04-22 14:50:00'),

(105, '22222222-2222-2222-2222-222222222222', 21, 'assistant',
 '您的症状（餐后上腹痛、反酸、嗳气）高度提示功能性消化不良或慢性胃炎。建议先做幽门螺杆菌呼气试验，如果阳性则根除治疗。若症状反复或您想明确诊断，可以预约胃镜检查。同时注意规律饮食，避免辛辣刺激食物，可暂时服用奥美拉唑缓解症状。',
 '[{"doc_id":"D2002","title":"消化不良的鉴别诊断","score":0.91,"snippet":"胃镜是诊断胃部疾病的金标准..."}]',
 '2025-04-22 14:58:00'),

-- 会话3: 高血压用药咨询
(106, '33333333-3333-3333-3333-333333333333', 22, 'user',
 '高血压5年了，一直吃硝苯地平控释片30mg每天一次，最近量血压还是150/95，睡觉时有时心慌，该怎么办？',
 '[{"doc_id":"D3001","title":"高血压联合用药策略","score":0.96,"snippet":"单药控制不佳时，可增加普利类或沙坦类..."}]',
 '2025-04-24 10:00:00'),

(107, '33333333-3333-3333-3333-333333333333', 22, 'assistant',
 '您的血压未达标（目标<140/90），且出现心慌，建议：1) 需要调整降压方案，可联合使用血管紧张素转换酶抑制剂（如培哚普利）或血管紧张素II受体拮抗剂（如氯沙坦）；2) 检查心电图、血电解质和肾功能；3) 限盐（每日<5g）、规律运动。请尽快到心内科专科调整用药，不要自行加药。',
 '[{"doc_id":"D3002","title":"高血压合并心率增快","score":0.89,"snippet":"心率偏快可考虑添加β受体阻滞剂..."}]',
 '2025-04-24 10:12:00'),

(108, '33333333-3333-3333-3333-333333333333', 22, 'user',
 '谢谢医生，我之前没有做过心电图，会尽快去心内科。另外我平时可以喝咖啡吗？',
 NULL,
 '2025-04-24 10:25:00'),

(109, '33333333-3333-3333-3333-333333333333', 22, 'assistant',
 '咖啡因可能引起血压一过性升高和心率加快，在血压未达标阶段建议不喝或少喝。如需提神，可选低因咖啡或茶。同时监测家庭血压，记录好数值带给医生。祝您早日控制平稳！',
 '[{"doc_id":"D3003","title":"高血压患者生活方式","score":0.83,"snippet":"限制咖啡因摄入，每日咖啡不超过1杯..."}]',
 '2025-04-24 10:30:00'),

(110, '33333333-3333-3333-3333-333333333333', 22, 'user',
 '好的，我明白了，非常感谢您的详细解答！',
 NULL,
 '2025-04-24 11:45:00');


-- ============================================================================
-- 完成！数据库初始化脚本执行完毕
-- ============================================================================
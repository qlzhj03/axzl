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

use axzl;

-- ----------------------------
-- 1、知识库元数据表
-- ----------------------------
drop table if exists biz_knowledge_meta;
create table biz_knowledge_meta (
  id                bigint(20)      not null auto_increment    comment '知识ID',
  question          text            not null                   comment '问题/文本块内容',
  answer            text                                       comment '答案/补充内容',
  label             varchar(50)     default ''                 comment '分类标签',
  score             int(11)         default 0                  comment '评分/重要度',
  related_diseases  varchar(500)    default ''                 comment '关联疾病',
  vector_id         varchar(100)    default ''                 comment 'Milvus向量ID',
  vector_status     char(1)         default '0'                comment '向量状态(0未导入 1已导入 2失败)',
  source_type       varchar(20)     default 'qa'               comment '来源类型(qa/doc)',
  source_id         bigint(20)      default null               comment '来源文档ID',
  chunk_index       int(11)         default 0                  comment '分块序号',
  source            varchar(100)    default ''                 comment '数据来源',
  status            char(1)         default '0'                comment '状态(0正常 1停用)',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志(0存在 2删除)',
  remark            varchar(500)    default null               comment '备注',
  primary key (id),
  key idx_vector_id (vector_id),
  key idx_label (label),
  key idx_source_type (source_type)
) engine=innodb auto_increment=100 comment = '知识库元数据表';

-- ----------------------------
-- 2、上传文档表
-- ----------------------------
drop table if exists biz_document;
create table biz_document (
  id                  bigint(20)      not null auto_increment    comment '文档ID',
  file_name           varchar(200)    not null                   comment '原始文件名',
  file_type           varchar(20)     not null                   comment '文件类型(txt/word/pdf/md)',
  file_size           bigint(20)      default 0                  comment '文件大小(字节)',
  milvus_file_path    varchar(500)    default ''                 comment 'Milvus对象存储路径',
  extracted_content   longtext                                   comment '提取的文本内容',
  chunk_count         int(11)         default 0                  comment '切分块数',
  vector_status       char(1)         default '0'                comment '向量状态(0未处理 1处理中 2已完成 3失败)',
  upload_by           bigint(20)      not null                   comment '上传用户ID',
  status              char(1)         default '0'                comment '状态(0正常 1删除)',
  create_time         datetime                                   comment '上传时间',
  update_time         datetime                                   comment '更新时间',
  del_flag            char(1)         default '0'                comment '删除标志(0存在 2删除)',
  remark              varchar(500)    default null               comment '备注',
  primary key (id),
  key idx_upload_by (upload_by),
  key idx_vector_status (vector_status)
) engine=innodb auto_increment=100 comment = '上传文档表';

-- ----------------------------
-- 3、问诊会话表
-- ----------------------------
drop table if exists biz_chat_session;
create table biz_chat_session (
  session_id          varchar(64)     not null                   comment '会话ID(UUID)',
  user_id             bigint(20)      not null                   comment '用户ID',
  title               varchar(200)    default ''                 comment '会话标题',
  symptom_summary     text                                       comment '症状摘要',
  ai_diagnosis        text                                       comment 'AI诊断摘要',
  recommended_label   varchar(50)     default ''                 comment '推荐科室标签',
  message_count       int(11)         default 0                  comment '消息数量',
  satisfaction        tinyint(1)      default null               comment '满意度(1-5星)',
  tokens_used         int(11)         default 0                  comment '消耗Token数',
  last_message_time   datetime                                   comment '最后消息时间',
  status              char(1)         default '0'                comment '状态(0进行中 1已结束)',
  create_time         datetime                                   comment '创建时间',
  update_time         datetime                                   comment '更新时间',
  del_flag            char(1)         default '0'                comment '删除标志(0存在 2删除)',
  remark              varchar(500)    default null               comment '备注',
  primary key (session_id),
  key idx_user_id (user_id),
  key idx_create_time (create_time)
) engine=innodb comment = '问诊会话表';

-- ----------------------------
-- 4、对话消息表
-- ----------------------------
drop table if exists biz_chat_message;
create table biz_chat_message (
  message_id          bigint(20)      not null auto_increment    comment '消息ID',
  session_id          varchar(64)     not null                   comment '会话ID',
  user_id             bigint(20)      not null                   comment '用户ID',
  role                varchar(20)     not null                   comment '角色(user/assistant)',
  content             text            not null                   comment '消息内容',
  retrieval_results   json                                       comment '向量检索结果(Top5)',
  create_time         datetime                                   comment '创建时间',
  primary key (message_id),
  key idx_session_id (session_id),
  key idx_create_time (create_time)
) engine=innodb auto_increment=100 comment = '对话消息表';

-- ----------------------------
-- 5、用户健康档案表
-- ----------------------------
drop table if exists biz_user_profile;
create table biz_user_profile (
  profile_id          bigint(20)      not null auto_increment    comment '档案ID',
  user_id             bigint(20)      not null                   comment '用户ID',
  birthday            date                                       comment '出生日期',
  gender              char(1)         default '0'                comment '性别(0男 1女 2未知)',
  blood_type          varchar(10)     default ''                 comment '血型',
  height              decimal(5,1)    default null               comment '身高(cm)',
  weight              decimal(5,1)    default null               comment '体重(kg)',
  allergy_history     text                                       comment '过敏史',
  medical_history     text                                       comment '既往病史',
  family_history      text                                       comment '家族病史',
  lifestyle           text                                       comment '生活习惯',
  status              char(1)         default '0'                comment '状态(0正常 1停用)',
  create_time         datetime                                   comment '创建时间',
  update_time         datetime                                   comment '更新时间',
  del_flag            char(1)         default '0'                comment '删除标志(0存在 2删除)',
  remark              varchar(500)    default null               comment '备注',
  primary key (profile_id),
  unique key uk_user_id (user_id)
) engine=innodb auto_increment=100 comment = '用户健康档案表';

-- ----------------------------
-- 菜单数据（业务模块）
-- ----------------------------
-- 一级菜单：业务管理
insert into sys_menu values('4', '业务管理', '0', '4', 'biz', null, '', '', 1, 0, 'M', '0', '0', '', 'guide', 'admin', sysdate(), '', null, '业务管理目录');

-- 二级菜单：知识库管理
insert into sys_menu values('401', '知识库管理', '4', '1', 'knowledge', 'biz/knowledge/index', '', '', 1, 0, 'C', '0', '0', 'biz:knowledge:list', 'education', 'admin', sysdate(), '', null, '知识库管理菜单');
-- 知识库管理按钮
insert into sys_menu values('4001', '知识查询', '401', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:knowledge:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4002', '知识新增', '401', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:knowledge:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4003', '知识修改', '401', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:knowledge:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4004', '知识删除', '401', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:knowledge:remove', '#', 'admin', sysdate(), '', null, '');

-- 二级菜单：文档管理
insert into sys_menu values('402', '文档管理', '4', '2', 'document', 'biz/document/index', '', '', 1, 0, 'C', '0', '0', 'biz:document:list', 'documentation', 'admin', sysdate(), '', null, '文档管理菜单');
-- 文档管理按钮
insert into sys_menu values('4005', '文档查询', '402', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:document:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4006', '文档处理', '402', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:document:process', '#', 'admin', sysdate(), '', null, '');

-- 二级菜单：问诊记录
insert into sys_menu values('403', '问诊记录', '4', '3', 'consultation', 'biz/consultation/index', '', '', 1, 0, 'C', '0', '0', 'biz:consultation:list', 'message', 'admin', sysdate(), '', null, '问诊记录菜单');
-- 问诊记录管理按钮
insert into sys_menu values('4007', '问诊查询', '403', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4008', '问诊删除', '403', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:remove', '#', 'admin', sysdate(), '', null, '');


-- 二级菜单：健康档案管理
insert into sys_menu values('404', '健康档案管理', '4', '4', 'profile', 'biz/profile/index', '', '', 1, 0, 'C', '0', '0', 'biz:profile:list', 'profile', 'admin', sysdate(), '', null, '接口档案管理菜单');
-- 健康档案管理按钮
insert into sys_menu values('4009', '档案新增', '404', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4010', '档案修改', '404', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4011', '档案查询', '404', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4012', '档案删除', '404', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:profile:remove', '#', 'admin', sysdate(), '', null, '');




-- 插入10条用户健康档案测试数据
INSERT INTO `biz_user_profile` (
    `user_id`, `birthday`, `gender`, `blood_type`, `height`, `weight`,
    `allergy_history`, `medical_history`, `family_history`, `lifestyle`,
    `status`, `create_time`, `update_time`, `del_flag`, `remark`
) VALUES
      (1, '1990-05-12', '0', 'A', 175.5, 72.3, '青霉素过敏', '无', '母亲高血压', '偶尔吸烟，每周运动2次', '0', '2024-01-10 08:30:00', '2025-04-20 14:25:00', '0', '体检正常'),
      (2, '1985-11-23', '1', 'B', 162.0, 55.8, '花粉过敏', '甲状腺功能减退', '父亲糖尿病', '不吸烟，规律瑜伽', '0', '2024-02-15 10:15:00', '2025-04-22 09:10:00', '0', '需定期复查甲功'),
      (3, '2000-07-08', '0', 'O', 180.2, 80.5, '无', '无', '无', '运动员，每天锻炼', '0', '2024-03-20 12:00:00', '2025-04-18 16:45:00', '0', '身体健康'),
      (4, '1978-12-01', '1', 'AB', 160.0, 68.0, '海鲜过敏', '高血压，高血脂', '母亲冠心病', '不吸烟，偶尔饮酒', '0', '2024-01-05 09:00:00', '2025-04-21 11:20:00', '0', '长期服药控制血压'),
      (5, '1995-03-19', '0', 'A', 170.0, 65.2, '无', '哮喘', '父亲哮喘', '吸烟10年，不饮酒', '0', '2024-04-10 14:30:00', '2025-04-19 13:40:00', '0', '哮喘控制良好'),
      (6, '1982-09-25', '1', 'B', 165.5, 71.0, '无', '2型糖尿病', '祖母糖尿病', '规律作息，不烟酒', '0', '2024-05-18 11:00:00', '2025-04-23 08:00:00', '0', '血糖控制中'),
      (7, '2005-04-14', '0', 'O', 172.0, 60.0, '尘螨过敏', '无', '无', '学生，久坐少动', '0', '2024-06-22 09:45:00', '2025-04-24 15:30:00', '0', '建议增加运动'),
      (8, '1970-06-30', '1', 'AB', 158.0, 52.0, '无', '骨质疏松', '母亲骨质疏松', '不吸烟，每日散步', '0', '2024-07-11 13:20:00', '2025-04-20 10:10:00', '0', '补充钙剂'),
      (9, '1998-10-17', '0', 'A', 185.0, 85.0, '磺胺类药物过敏', '无', '无', '健身爱好者，高蛋白饮食', '0', '2024-08-05 16:00:00', '2025-04-22 14:00:00', '0', '体型健硕'),
      (10, '1988-02-29', '2', 'O', 168.0, 62.5, '无', '慢性胃炎', '父亲胃癌', '喜辛辣，三餐不规律', '0', '2024-09-19 08:00:00', '2025-04-25 09:30:00', '0', '建议胃镜检查'),
      (11, '1988-02-29', '2', 'O', 168.0, 62.5, '无', '慢性胃炎', '父亲胃癌', '喜辛辣，三餐不规律', '0', '2024-09-19 08:00:00', '2025-04-25 09:30:00', '0', '建议胃镜检查'),
      (12, '1988-02-29', '2', 'O', 168.0, 62.5, '无', '慢性胃炎', '父亲胃癌', '喜辛辣，三餐不规律', '0', '2024-09-19 08:00:00', '2025-04-25 09:30:00', '0', '建议胃镜检查'),
      (13, '1988-02-29', '2', 'O', 168.0, 62.5, '无', '慢性胃炎', '父亲胃癌', '喜辛辣，三餐不规律', '0', '2024-09-19 08:00:00', '2025-04-25 09:30:00', '0', '建议胃镜检查');

-- ----------------------------
-- 问诊会话测试数据 (biz_chat_session)
-- ----------------------------
INSERT INTO `biz_chat_session` (
    `session_id`, `user_id`, `title`, `symptom_summary`, `ai_diagnosis`, `recommended_label`,
    `message_count`, `satisfaction`, `tokens_used`, `last_message_time`, `status`,
    `create_time`, `update_time`, `del_flag`, `remark`
) VALUES
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

-- ----------------------------
-- 对话消息测试数据 (biz_chat_message)
-- 注意：message_id 自增从 100 开始，需手动指定或让数据库自动生成
-- 此处为清晰阅读，明确指定了 message_id 值（100~108）
-- ----------------------------
INSERT INTO `biz_chat_message` (
    `message_id`, `session_id`, `user_id`, `role`, `content`,
    `retrieval_results`, `create_time`
) VALUES
-- 会话1（感冒咳嗽）消息：user -> assistant -> user -> assistant
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
 '复方氨酚烷胺片可以缓解感冒症状，按说明书服用即可。普通感冒多为病毒性，不建议自行服用“消炎药”（即抗生素），除非合并细菌感染证据（如黄脓痰、白细胞升高等）。如果症状加重请及时复诊。',
 '[{"doc_id":"D1003","title":"抗菌药物合理使用","score":0.85,"snippet":"普通感冒避免常规使用抗生素..."}]',
 '2025-04-20 09:18:00'),

-- 会话2（胃痛咨询）消息：user -> assistant
(104, '22222222-2222-2222-2222-222222222222', 2, 'user',
 '医生，我最近一周上腹隐痛，吃完饭更难受，还经常反酸打嗝，大便颜色正常。请问是什么问题？需要做胃镜吗？',
 '[{"doc_id":"D2001","title":"慢性胃炎诊治","score":0.94,"snippet":"餐后上腹痛、反酸是慢性胃炎典型表现..."}]',
 '2025-04-22 14:50:00'),

(105, '22222222-2222-2222-2222-222222222222', 2, 'assistant',
 '您的症状（餐后上腹痛、反酸、嗳气）高度提示功能性消化不良或慢性胃炎。建议先做幽门螺杆菌呼气试验，如果阳性则根除治疗。若症状反复或您想明确诊断，可以预约胃镜检查。同时注意规律饮食，避免辛辣刺激食物，可暂时服用奥美拉唑缓解症状。',
 '[{"doc_id":"D2002","title":"消化不良的鉴别诊断","score":0.91,"snippet":"胃镜是诊断胃部疾病的金标准..."}]',
 '2025-04-22 14:58:00'),

-- 会话3（高血压用药咨询）消息：user -> assistant -> user -> assistant -> user
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

-- ----------------------------
-- 更新会话表中的 message_count 和 last_message_time （确保统计准确）
-- ----------------------------
UPDATE biz_chat_session SET
                            message_count = 4,
                            last_message_time = '2025-04-20 09:18:00'
WHERE session_id = '11111111-1111-1111-1111-111111111111';

UPDATE biz_chat_session SET
                            message_count = 2,
                            last_message_time = '2025-04-22 14:58:00'
WHERE session_id = '22222222-2222-2222-2222-222222222222';

UPDATE biz_chat_session SET
                            message_count = 5,
                            last_message_time = '2025-04-24 11:45:00'
WHERE session_id = '33333333-3333-3333-3333-333333333333';

-- ----------------------------
-- 问诊测试菜单配置
-- ----------------------------
-- 在"业务管理"下添加"问诊测试"菜单项
insert into sys_menu values('405', '问诊测试', '4', '5', 'consultation-test', 'biz/consultation/ConsultationTest', '', '', 1, 0, 'C', '0', '0', 'biz:consultation:test', 'chat-dot-round', 'admin', sysdate(), '', null, '问诊测试菜单');

-- 添加流式对话权限
insert into sys_menu values('4013', '流式对话', '403', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:consultation:chat', '#', 'admin', sysdate(), '', null, '');

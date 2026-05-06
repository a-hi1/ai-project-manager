-- =============================================
-- 演示数据初始化脚本
-- =============================================
-- 注意：此脚本需要在init.sql之后执行
-- =============================================

BEGIN;

\echo '开始导入演示数据...'

-- =============================================
-- 项目1：AI智能办公系统（进行中）
-- =============================================
\echo '  - 插入项目1...'

INSERT INTO project (name, description, start_date, end_date, status, created_by) VALUES
('AI智能办公系统', '开发一款基于人工智能的办公自动化系统，包含智能日程管理、邮件分类、文档自动归档等功能', '2026-01-01', '2026-06-30', 'in_progress', 2);

-- 添加项目1成员
INSERT INTO project_member (project_id, user_id, role) VALUES
(1, 2, '项目经理'),
(1, 3, '开发'),
(1, 4, '测试');

-- 添加项目1的任务
INSERT INTO task (project_id, name, description, start_date, end_date, duration, progress, priority, status, level, assigned_to, created_by) VALUES
(1, '项目启动与需求分析', '完成项目启动会议，确定项目范围和目标', '2026-01-01', '2026-01-15', 15, 100, 'high', 'done', 0, 2, 2),
(1, '技术架构设计', '设计系统技术架构，选择技术栈', '2026-01-10', '2026-01-25', 15, 100, 'high', 'done', 0, 3, 2),
(1, '智能日程管理模块', '开发智能日程管理功能', '2026-01-20', '2026-02-28', 40, 75, 'high', 'in_progress', 0, 3, 2),
(1, '邮件智能分类功能', '实现基于AI的邮件自动分类', '2026-02-15', '2026-03-31', 45, 50, 'medium', 'in_progress', 0, 3, 2),
(1, '文档自动归档系统', '开发文档智能分类和归档功能', '2026-03-01', '2026-04-15', 45, 20, 'medium', 'todo', 0, 3, 2),
(1, '前端界面开发', '开发用户界面和交互', '2026-01-25', '2026-04-30', 95, 40, 'high', 'in_progress', 0, 3, 2),
(1, '系统测试', '进行功能测试和集成测试', '2026-04-01', '2026-05-31', 60, 0, 'high', 'todo', 0, 4, 2),
(1, '部署上线', '部署系统到生产环境', '2026-06-01', '2026-06-30', 30, 0, 'high', 'todo', 0, 3, 2);

-- 添加项目1的子任务
INSERT INTO task (project_id, parent_id, name, description, start_date, end_date, duration, progress, priority, status, level, assigned_to, created_by) VALUES
(1, 3, '日程数据模型设计', '设计日程相关的数据表结构', '2026-01-20', '2026-01-31', 10, 100, 'high', 'done', 1, 3, 2),
(1, 3, '日历组件开发', '开发日历展示组件', '2026-02-01', '2026-02-15', 15, 80, 'medium', 'in_progress', 1, 3, 2),
(1, 3, '智能冲突检测', '实现日程冲突自动检测', '2026-02-10', '2026-02-28', 18, 50, 'medium', 'todo', 1, 3, 2);

-- 添加项目1的风险
INSERT INTO risk (project_id, name, description, probability, impact, category, status, mitigation_plan, contingency_plan, identified_by, identified_at) VALUES
(1, 'AI模型准确性不足', '邮件分类和文档归档的AI模型准确率可能不达标', 30, 70, '技术风险', 'monitoring', '1. 收集更多训练数据\n2. 优化模型参数', '1. 引入第三方AI服务\n2. 提供人工校正功能', 2, '2026-01-15'),
(1, '人员离职风险', '核心开发人员可能离职', 20, 60, '人员风险', 'monitoring', '1. 做好知识管理\n2. 交叉培训', '1. 快速招聘替代人员\n2. 调整项目计划', 2, '2026-01-10'),
(1, '进度延期风险', '需求变更可能导致进度延期', 40, 50, '进度风险', 'monitoring', '1. 严格执行变更管理\n2. 预留缓冲时间', '1. 削减非核心功能\n2. 增加资源', 2, '2026-01-20');

-- 添加项目1的里程碑
INSERT INTO milestone (project_id, name, description, due_date, status, created_by) VALUES
(1, 'Alpha版本发布', '完成核心功能开发，发布内部测试版本', '2026-03-31', 'pending', 2),
(1, 'Beta版本发布', '完成功能开发，发布公测版本', '2026-05-15', 'pending', 2),
(1, '正式上线', '系统正式发布上线', '2026-06-30', 'pending', 2);

-- 添加项目1的缺陷
INSERT INTO bug (project_id, title, description, severity, priority, status, reporter_id, assignee_id) VALUES
(1, '日历组件日期显示错误', '切换月份时日期显示不正确', 'minor', 'low', 'resolved', 4, 3),
(1, '邮件分类准确率低', '某些邮件被错误分类', 'major', 'high', 'assigned', 4, 3),
(1, '文档上传失败', '大文件上传时出现超时', 'minor', 'medium', 'open', 4, 3);

-- 添加工时记录
INSERT INTO work_log (task_id, user_id, hours, work_date, description) VALUES
(1, 2, 8, '2026-01-05', '参加项目启动会议'),
(1, 2, 4, '2026-01-08', '编写项目计划'),
(2, 3, 8, '2026-01-12', '技术选型讨论'),
(2, 3, 6, '2026-01-15', '编写架构设计文档'),
(3, 3, 7, '2026-01-22', '日程模块需求分析'),
(3, 3, 8, '2026-01-25', '日历组件开发');

-- 添加变更请求
INSERT INTO change_request (project_id, title, description, reason, impact, status, requester_id, reviewer_id, reviewed_at) VALUES
(1, '增加语音输入功能', '用户希望能通过语音快速创建日程', '提升用户体验', '需要增加语音识别模块，预计增加5人天工作量', 'approved', 2, 2, '2026-01-20'),
(1, '调整文档归档算法', '当前算法分类效果不佳', '提升归档准确性', '需要重新训练模型，预计增加10人天', 'pending', 3, NULL, NULL);

-- 添加需求调研
INSERT INTO requirement_research (project_id, title, description, research_method, research_result, creator_id) VALUES
(1, '用户调研', '调研目标用户的办公自动化需求', '问卷调研+用户访谈', '用户最关注日程管理和邮件分类功能', 2),
(1, '竞品分析', '分析市场上的同类产品', '竞品对比分析', '主要竞品功能全面但价格较高，我们有机会在性价比上取胜', 2);

-- 添加可行性分析
INSERT INTO feasibility_analysis (project_id, title, technical_feasibility, economic_feasibility, market_feasibility, risk_assessment, conclusion, creator_id) VALUES
(1, 'AI办公系统可行性', '已有成熟的NLP和CV技术支撑', '预计投入150万，预期收益300万', '办公自动化市场需求旺盛', '技术风险中等，需注意AI模型训练', '可行', 2);

-- =====================================================
-- 创建演示项目2：电商导购系统（已完成）
-- =====================================================
INSERT INTO project (name, description, start_date, end_date, status, created_by) VALUES
('电商导购系统', '开发一款智能化电商导购平台，提供个性化商品推荐、智能比价、购物攻略等功能', '2025-06-01', '2025-12-31', 'completed', 1);

-- 添加项目2成员
INSERT INTO project_member (project_id, user_id, role) VALUES
(2, 1, '项目经理'),
(2, 2, '项目经理'),
(2, 3, '开发'),
(2, 4, '测试');

-- 添加项目2的任务（已完成）
INSERT INTO task (project_id, name, description, start_date, end_date, duration, progress, priority, status, level, assigned_to, created_by) VALUES
(2, '项目启动与需求分析', '完成项目启动会议，确定项目范围', '2025-06-01', '2025-06-15', 15, 100, 'high', 'done', 0, 1, 1),
(2, '系统架构设计', '设计系统整体架构和技术选型', '2025-06-10', '2025-06-25', 15, 100, 'high', 'done', 0, 3, 1),
(2, '数据库设计', '完成数据库表结构设计和创建', '2025-06-15', '2025-06-30', 15, 100, 'high', 'done', 0, 3, 1),
(2, '用户中心模块', '开发用户注册、登录、个人中心', '2025-07-01', '2025-07-31', 30, 100, 'high', 'done', 0, 3, 1),
(2, '商品管理模块', '商品展示、搜索、筛选功能', '2025-07-15', '2025-08-31', 45, 100, 'high', 'done', 0, 3, 1),
(2, '个性化推荐系统', '基于用户行为的智能推荐', '2025-08-01', '2025-09-30', 60, 100, 'high', 'done', 0, 3, 1),
(2, '购物车与订单', '购物车、订单流程、支付集成', '2025-09-01', '2025-10-15', 45, 100, 'high', 'done', 0, 3, 1),
(2, '智能比价功能', '同类商品多平台价格对比', '2025-09-15', '2025-10-31', 45, 100, 'medium', 'done', 0, 3, 1),
(2, '用户评价系统', '商品评价、晒单、问答', '2025-10-01', '2025-10-31', 30, 100, 'medium', 'done', 0, 3, 1),
(2, '系统测试与优化', '功能测试、性能优化', '2025-11-01', '2025-11-30', 30, 100, 'high', 'done', 0, 4, 1),
(2, '部署上线', '生产环境部署和上线', '2025-12-01', '2025-12-31', 30, 100, 'high', 'done', 0, 3, 1);

-- 添加项目2的风险
INSERT INTO risk (project_id, name, description, probability, impact, category, status, mitigation_plan, contingency_plan, identified_by, identified_at) VALUES
(2, '第三方API不稳定', '比价功能依赖的第三方价格API可能不稳定', 30, 50, '技术风险', 'resolved', '1. 接入多家API\n2. 本地缓存价格数据', '1. 降低比价更新频率\n2. 手动维护价格库', 1, '2025-06-15'),
(2, '推荐算法效果不佳', '个性化推荐准确率可能不达预期', 40, 60, '技术风险', 'resolved', '1. 采用成熟的推荐算法\n2. AB测试优化', '1. 简化推荐规则\n2. 增加人工运营推荐', 1, '2025-08-01'),
(2, '大促期间性能问题', '双11等活动期间流量激增', 50, 70, '运维风险', 'resolved', '1. 提前扩容\n2. 准备降级方案', '1. 关闭非核心功能\n2. 引导用户错峰访问', 1, '2025-10-15');

-- 添加项目2的里程碑
INSERT INTO milestone (project_id, name, description, due_date, status, created_by) VALUES
(2, 'MVP版本发布', '完成核心功能，发布最小可用版本', '2025-09-01', 'completed', 1),
(2, 'Beta版本发布', '完成全部功能，发布公测版本', '2025-11-15', 'completed', 1),
(2, '正式上线', '系统正式发布上线', '2025-12-31', 'completed', 1);

-- 添加项目2的缺陷
INSERT INTO bug (project_id, title, description, severity, priority, status, reporter_id, assignee_id, resolved_at) VALUES
(2, '商品图片加载慢', '商品列表图片加载速度慢', 'minor', 'low', 'resolved', 4, 3, '2025-11-05'),
(2, '搜索结果不准确', '关键词搜索结果与预期不符', 'major', 'high', 'resolved', 4, 3, '2025-10-20'),
(2, '支付回调超时', '部分支付渠道回调处理超时', 'critical', 'high', 'resolved', 4, 3, '2025-11-10'),
(2, '推荐商品重复', '推荐列表出现重复商品', 'minor', 'medium', 'resolved', 4, 3, '2025-10-25');

-- 添加工时记录
INSERT INTO work_log (task_id, user_id, hours, work_date, description) VALUES
(12, 1, 8, '2025-06-01', '参加项目启动会议'),
(12, 1, 4, '2025-06-05', '编写项目计划'),
(13, 3, 8, '2025-06-12', '技术架构讨论'),
(13, 3, 6, '2025-06-18', '编写架构设计文档'),
(14, 3, 8, '2025-06-20', '数据库设计'),
(14, 3, 6, '2025-06-25', '创建数据库表'),
(15, 3, 8, '2025-07-05', '用户模块开发'),
(15, 3, 8, '2025-07-12', '登录功能实现'),
(16, 3, 8, '2025-07-20', '商品模块开发'),
(16, 3, 8, '2025-07-28', '商品搜索功能'),
(17, 3, 8, '2025-08-10', '推荐算法实现'),
(17, 3, 8, '2025-08-20', '推荐接口优化'),
(18, 3, 8, '2025-09-10', '购物车开发'),
(18, 3, 8, '2025-09-20', '订单流程开发'),
(19, 3, 8, '2025-09-25', '比价功能开发'),
(20, 3, 8, '2025-10-10', '评价系统开发'),
(21, 4, 8, '2025-11-05', '功能测试'),
(21, 4, 8, '2025-11-15', '性能测试'),
(22, 3, 8, '2025-12-05', '部署上线');

-- 添加项目2的复盘报告
INSERT INTO project_retrospective (
    project_id, summary, duration_days, total_tasks, completed_tasks,
    task_completion_rate, total_risks, resolved_risks, total_bugs, resolved_bugs,
    total_hours, key_achievements, key_challenges, lessons_learned, recommendations, created_by
) VALUES (
    2,
    '电商导购系统项目圆满完成，成功上线并获得用户好评。项目采用敏捷开发模式，历时7个月，完成11个模块的开发，系统稳定运行。',
    213,
    11,
    11,
    100.00,
    3,
    3,
    4,
    4,
    1680.00,
    '1. 成功实现个性化推荐系统，推荐准确率达75%\n2. 智能比价功能接入5家主流电商平台\n3. 系统支持日均10万+访问量\n4. 用户满意度评分4.8/5.0',
    '1. 第三方API不稳定影响比价功能\n2. 推荐算法初期效果不理想\n3. 大促期间性能压力测试不充分',
    '1. 第三方服务必须做好容错和降级方案\n2. 算法类功能需要更多时间迭代优化\n3. 性能测试要提前做，留足优化时间',
    '1. 后续项目增加技术预研阶段\n2. 建议引入推荐算法团队专维护\n3. 建立完整的性能测试机制',
    1
);

-- 添加通知数据
INSERT INTO notification (user_id, title, content, type, read_status, create_time) VALUES
-- 用户1的通知
(1, '项目完成', '电商导购系统项目已成功上线，感谢团队的辛勤付出！', 'project_status', false, '2025-12-31 10:00:00'),
(1, '新任务分配', '您被分配了新任务：AI模型优化，项目：AI智能办公系统', 'task_assigned', false, '2026-04-28 09:00:00'),
(1, '高风险预警', '项目 AI智能办公系统 中发现高风险项：AI模型准确性不足', 'risk_alert', true, '2026-04-25 14:00:00'),
(1, '里程碑提醒', '项目 AI智能办公系统 的里程碑 Alpha版本发布 即将到达', 'milestone', false, '2026-04-29 08:00:00'),

-- 用户2的通知
(2, '任务进度更新', '任务 智能日程管理模块 进度更新为 75%', 'task_progress', false, '2026-04-28 16:00:00'),
(2, '高风险预警', '项目 AI智能办公系统 中发现高风险项：进度延期风险', 'risk_alert', false, '2026-04-27 11:00:00'),
(2, '变更请求待审批', '项目 AI智能办公系统 有新的变更请求：调整文档归档算法，需要您审批', 'change_request', false, '2026-04-26 10:00:00'),
(2, '项目状态变更', '项目 电商导购系统 状态已变更为：completed', 'project_status', true, '2025-12-31 10:30:00'),

-- 用户3的通知
(3, '任务完成', '恭喜！您负责的所有任务都已完成验收。', 'task_assigned', true, '2025-12-30 15:00:00'),
(3, '新任务分配', '您被分配了新任务：系统测试，项目：AI智能办公系统', 'task_assigned', false, '2026-04-28 10:00:00'),
(3, '任务进度更新', '任务 邮件智能分类功能 进度更新为 50%', 'task_progress', false, '2026-04-27 14:00:00'),
(3, '里程碑提醒', '项目 AI智能办公系统 的里程碑 Beta版本发布 即将到达', 'milestone', false, '2026-04-29 08:30:00'),

-- 用户4的通知
(4, '新任务分配', '您被分配了新任务：系统测试，项目：AI智能办公系统', 'task_assigned', false, '2026-04-28 10:30:00'),
(4, '任务进度更新', '任务 日历组件开发 进度更新为 80%', 'task_progress', true, '2026-04-26 15:00:00'),
(4, '高风险预警', '项目 AI智能办公系统 中发现高风险项：人员离职风险', 'risk_alert', false, '2026-04-25 09:00:00');

-- =============================================
-- 知识库演示数据
-- =============================================
\echo '  - 插入知识库数据...'

INSERT INTO knowledge_document (project_id, user_id, title, content, doc_type, created_by, created_at) VALUES
(1, 2, '项目管理最佳实践', '
项目管理最佳实践指南：

1. 需求管理
   - 明确项目范围，避免范围蔓延
   - 定期评审需求变更
   - 建立需求追踪矩阵

2. 风险管理
   - 定期进行风险识别和评估
   - 制定风险应对计划
   - 持续监控风险状态

3. 质量管理
   - 建立质量标准
   - 定期进行代码审查
   - 实施自动化测试

4. 沟通管理
   - 每周召开项目例会
   - 及时更新项目状态
   - 建立问题升级机制

5. 进度管理
   - 使用甘特图跟踪进度
   - 设置里程碑节点
   - 及时调整项目计划
', 'management', 2, CURRENT_TIMESTAMP),

(1, 2, '智能办公系统技术架构', '
智能办公系统技术架构设计：

1. 前端技术栈
   - Vue 3 + TypeScript
   - Element Plus UI框架
   - Pinia状态管理
   - Vue Router路由

2. 后端技术栈
   - Spring Boot 3.x
   - MyBatis Plus ORM
   - PostgreSQL数据库
   - Redis缓存

3. AI集成方案
   - 使用智谱AI大模型
   - LangChain4j框架
   - pgvector向量存储
   - RAG检索增强生成

4. 核心模块
   - 用户认证与授权
   - 智能日程管理
   - 邮件智能分类
   - 文档自动归档
   - 项目管理助手
', 'technical', 2, CURRENT_TIMESTAMP),

(1, 2, '需求文档-智能日程管理', '
智能日程管理模块需求说明：

功能需求：
1. 日程创建与编辑
   - 支持添加标题、描述、时间、地点
   - 支持设置重复规则
   - 支持添加参与者

2. 智能冲突检测
   - 自动检测日程时间冲突
   - 提供冲突解决方案建议

3. 日程提醒
   - 支持多种提醒方式
   - 自定义提醒时间

4. 日程共享
   - 支持团队共享日程
   - 权限控制

5. AI助手集成
   - 语音输入创建日程
   - 智能日程建议
   - 自动日程整理
', 'requirement', 2, CURRENT_TIMESTAMP),

(2, 1, '电商导购系统复盘总结', '
电商导购系统项目复盘总结：

项目概况：
- 项目周期：2025年6月-12月
- 项目状态：已完成并上线
- 团队规模：4人

主要成就：
1. 成功实现个性化推荐系统，推荐准确率达到75%
2. 智能比价功能接入5家主流电商平台
3. 系统支持日均10万+访问量
4. 用户满意度评分4.8/5.0

遇到的问题：
1. 第三方API不稳定，影响比价功能
2. 推荐算法初期效果不理想
3. 大促期间性能测试不充分

经验教训：
1. 第三方服务必须做好容错和降级方案
2. 算法类功能需要更多时间迭代优化
3. 性能测试要提前做，留足优化时间

后续建议：
1. 增加技术预研阶段
2. 引入推荐算法团队专维护
3. 建立完整的性能测试机制
', 'management', 1, CURRENT_TIMESTAMP);

-- =============================================
-- 提交事务
-- =============================================
COMMIT;

\echo ''
\echo '演示数据导入完成！'
\echo ''
\echo '数据统计：'
SELECT
    (SELECT COUNT(*) FROM project) as "项目数",
    (SELECT COUNT(*) FROM task) as "任务数",
    (SELECT COUNT(*) FROM project_member) as "项目成员数",
    (SELECT COUNT(*) FROM risk) as "风险数",
    (SELECT COUNT(*) FROM bug) as "缺陷数",
    (SELECT COUNT(*) FROM notification) as "通知数",
    (SELECT COUNT(*) FROM knowledge_document) as "知识库文档数";

-- 索引优化脚本（用于已有数据库的索引添加）
-- 运行此脚本为现有数据库添加缺失的索引

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_user_role_id ON user_info(role_id);
CREATE INDEX IF NOT EXISTS idx_user_username ON user_info(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON user_info(email);

-- 项目表索引
CREATE INDEX IF NOT EXISTS idx_project_created_by ON project(created_by);
CREATE INDEX IF NOT EXISTS idx_project_status ON project(status);

-- 项目成员表索引
CREATE INDEX IF NOT EXISTS idx_project_member_project_id ON project_member(project_id);
CREATE INDEX IF NOT EXISTS idx_project_member_user_id ON project_member(user_id);

-- 任务表索引（重点优化）
CREATE INDEX IF NOT EXISTS idx_task_project_id ON task(project_id);
CREATE INDEX IF NOT EXISTS idx_task_parent_id ON task(parent_id);
CREATE INDEX IF NOT EXISTS idx_task_assigned_to ON task(assigned_to);
CREATE INDEX IF NOT EXISTS idx_task_status ON task(status);
CREATE INDEX IF NOT EXISTS idx_task_level ON task(level);
CREATE INDEX IF NOT EXISTS idx_task_created_by ON task(created_by);

-- 任务依赖表索引
CREATE INDEX IF NOT EXISTS idx_task_dependency_task_id ON task_dependency(task_id);
CREATE INDEX IF NOT EXISTS idx_task_dependency_depends_on_id ON task_dependency(depends_on_id);

-- 风险表索引
CREATE INDEX IF NOT EXISTS idx_risk_project_id ON risk(project_id);
CREATE INDEX IF NOT EXISTS idx_risk_status ON risk(status);

-- 里程碑表索引
CREATE INDEX IF NOT EXISTS idx_milestone_project_id ON milestone(project_id);

-- 工时记录表索引
CREATE INDEX IF NOT EXISTS idx_work_log_task_id ON work_log(task_id);
CREATE INDEX IF NOT EXISTS idx_work_log_user_id ON work_log(user_id);

-- 缺陷表索引
CREATE INDEX IF NOT EXISTS idx_bug_project_id ON bug(project_id);
CREATE INDEX IF NOT EXISTS idx_bug_status ON bug(status);
CREATE INDEX IF NOT EXISTS idx_bug_assignee_id ON bug(assignee_id);

-- 变更请求表索引
CREATE INDEX IF NOT EXISTS idx_change_request_project_id ON change_request(project_id);
CREATE INDEX IF NOT EXISTS idx_change_request_status ON change_request(status);

-- 知识库文档索引
CREATE INDEX IF NOT EXISTS idx_knowledge_document_project_id ON knowledge_document(project_id);

-- AI对话历史索引
CREATE INDEX IF NOT EXISTS idx_ai_conversation_project_id ON ai_conversation(project_id);
CREATE INDEX IF NOT EXISTS idx_ai_conversation_user_id ON ai_conversation(user_id);

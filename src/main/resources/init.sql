-- =============================================
-- 项目管理系统数据库初始化脚本
-- 注意：此脚本可以安全重复运行，不会破坏现有数据
-- =============================================

-- 启用pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建角色表
CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建权限表
CREATE TABLE IF NOT EXISTS permission (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS user_info (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role_id INTEGER NOT NULL,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(id)
);

-- 插入初始角色（如果不存在）
INSERT INTO role (name, description) VALUES
('admin', 'System Administrator'),
('pm', 'Project Manager'),
('developer', 'Developer'),
('tester', 'Tester'),
('guest', 'Guest')
ON CONFLICT (name) DO NOTHING;

-- 插入初始权限（如果不存在）
INSERT INTO permission (name, description) VALUES
('user:manage', 'User Management'),
('project:create', 'Create Project'),
('project:manage', 'Manage Project'),
('task:create', 'Create Task'),
('task:manage', 'Manage Task'),
('report:view', 'View Report'),
('report:generate', 'Generate Report')
ON CONFLICT (name) DO NOTHING;

-- 分配权限给角色（如果不存在）
INSERT INTO role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7),
(2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7),
(3, 4), (3, 5), (3, 6),
(4, 4), (4, 5), (4, 6),
(5, 6)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 创建项目表
CREATE TABLE IF NOT EXISTS project (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'pending',
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES user_info(id)
);

-- 创建项目成员表
CREATE TABLE IF NOT EXISTS project_member (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
);

-- 添加唯一约束（如果不存在）
DO $$
BEGIN
    BEGIN
        ALTER TABLE project_member ADD CONSTRAINT uk_project_member_project_user UNIQUE (project_id, user_id);
    EXCEPTION
        WHEN duplicate_table OR duplicate_object THEN
            NULL;
    END;
END $$;

-- 创建需求调研表
CREATE TABLE IF NOT EXISTS requirement_research (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    research_method VARCHAR(100),
    research_result TEXT,
    creator_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES user_info(id)
);

-- 创建可行性分析表
CREATE TABLE IF NOT EXISTS feasibility_analysis (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    title VARCHAR(100) NOT NULL,
    technical_feasibility TEXT,
    economic_feasibility TEXT,
    market_feasibility TEXT,
    risk_assessment TEXT,
    conclusion VARCHAR(200),
    creator_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES user_info(id)
);

-- 创建任务表（WBS任务分解）
CREATE TABLE IF NOT EXISTS task (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    parent_id INTEGER,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    duration INTEGER,
    progress INTEGER DEFAULT 0,
    priority VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(20) DEFAULT 'todo',
    is_milestone BOOLEAN DEFAULT FALSE,
    path VARCHAR(500),
    level INTEGER DEFAULT 0,
    assigned_to INTEGER,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES task(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES user_info(id),
    FOREIGN KEY (created_by) REFERENCES user_info(id)
);

-- 创建任务依赖关系表
CREATE TABLE IF NOT EXISTS task_dependency (
    id SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL,
    depends_on_id INTEGER NOT NULL,
    dependency_type VARCHAR(20) DEFAULT 'finish_to_start',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    FOREIGN KEY (depends_on_id) REFERENCES task(id) ON DELETE CASCADE
);

-- 创建风险登记册表
CREATE TABLE IF NOT EXISTS risk (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    probability INTEGER DEFAULT 0,
    impact INTEGER DEFAULT 0,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'identified',
    mitigation_plan TEXT,
    contingency_plan TEXT,
    identified_by INTEGER NOT NULL,
    identified_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (identified_by) REFERENCES user_info(id)
);

-- 创建里程碑表
CREATE TABLE IF NOT EXISTS milestone (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    due_date DATE,
    status VARCHAR(20) DEFAULT 'pending',
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES user_info(id)
);

-- 创建任务状态日志表
CREATE TABLE IF NOT EXISTS task_status_log (
    id SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    changed_by INTEGER NOT NULL,
    comment TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES user_info(id)
);

-- 创建工时记录表
CREATE TABLE IF NOT EXISTS work_log (
    id SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    hours DECIMAL(10,2) NOT NULL,
    work_date DATE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_info(id)
);

-- 创建变更请求表
CREATE TABLE IF NOT EXISTS change_request (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    reason TEXT,
    impact TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    requester_id INTEGER NOT NULL,
    reviewer_id INTEGER,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (requester_id) REFERENCES user_info(id),
    FOREIGN KEY (reviewer_id) REFERENCES user_info(id)
);

-- 创建缺陷表
CREATE TABLE IF NOT EXISTS bug (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    task_id INTEGER,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    severity VARCHAR(20) DEFAULT 'medium',
    priority VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(20) DEFAULT 'open',
    reporter_id INTEGER NOT NULL,
    assignee_id INTEGER,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES task(id),
    FOREIGN KEY (reporter_id) REFERENCES user_info(id),
    FOREIGN KEY (assignee_id) REFERENCES user_info(id)
);

-- 创建交付物表
CREATE TABLE IF NOT EXISTS deliverable (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    file_name VARCHAR(500),
    submitted_by INTEGER NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'pending',
    reviewed_by INTEGER,
    reviewed_at TIMESTAMP,
    comments TEXT,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (submitted_by) REFERENCES user_info(id),
    FOREIGN KEY (reviewed_by) REFERENCES user_info(id)
);

-- 创建文档归档表
CREATE TABLE IF NOT EXISTS document_archive (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    doc_type VARCHAR(50),
    source_table VARCHAR(50),
    source_id INTEGER,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    archived_by INTEGER NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (archived_by) REFERENCES user_info(id)
);

-- 创建项目复盘表
CREATE TABLE IF NOT EXISTS project_retrospective (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    summary TEXT,
    duration_days INTEGER,
    total_tasks INTEGER,
    completed_tasks INTEGER,
    task_completion_rate DECIMAL(5,2),
    total_risks INTEGER,
    resolved_risks INTEGER,
    total_bugs INTEGER,
    resolved_bugs INTEGER,
    total_hours DECIMAL(10,2),
    key_achievements TEXT,
    key_challenges TEXT,
    lessons_learned TEXT,
    recommendations TEXT,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES user_info(id)
);

-- 为现有user_info表添加status字段（如果不存在）
ALTER TABLE user_info ADD COLUMN IF NOT EXISTS status INTEGER DEFAULT 1;

-- 插入初始用户（如果不存在，密码都是123456）
INSERT INTO user_info (username, password, email, role_id, status) VALUES
('admin', '123456', 'admin@example.com', 1, 1),
('pm', '123456', 'pm@example.com', 2, 1),
('developer', '123456', 'developer@example.com', 3, 1),
('tester', '123456', 'tester@example.com', 4, 1),
('guest', '123456', 'guest@example.com', 5, 1)
ON CONFLICT (username) DO NOTHING;

-- 确保 email 唯一约束也处理
ALTER TABLE user_info DROP CONSTRAINT IF EXISTS user_info_email_key;
ALTER TABLE user_info ADD CONSTRAINT user_info_email_key UNIQUE (email);

-- 创建通知表
CREATE TABLE IF NOT EXISTS notification (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(50) DEFAULT 'system',
    read_status BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
);

-- 通知表索引
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_read_status ON notification(read_status);
CREATE INDEX IF NOT EXISTS idx_notification_create_time ON notification(create_time DESC);

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    username VARCHAR(100),
    operation VARCHAR(100) NOT NULL,
    content TEXT,
    ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE SET NULL
);

-- 操作日志表索引
CREATE INDEX IF NOT EXISTS idx_operation_log_user_id ON operation_log(user_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_create_time ON operation_log(create_time DESC);

-- =========================================
-- AI 功能相关表
-- =========================================
-- 开发环境：如需干净重建，取消下面的注释
-- DROP TABLE IF EXISTS knowledge_document;
-- DROP TABLE IF EXISTS ai_conversation;

-- 创建AI对话表
CREATE TABLE IF NOT EXISTS ai_conversation (
    id SERIAL PRIMARY KEY,
    project_id INTEGER,
    user_id INTEGER,  -- 改为可空，便于开发
    role VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE SET NULL
);

-- 对话表索引
CREATE INDEX IF NOT EXISTS idx_ai_conversation_project_id ON ai_conversation(project_id);
CREATE INDEX IF NOT EXISTS idx_ai_conversation_user_id ON ai_conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_conversation_created_at ON ai_conversation(created_at DESC);

-- 创建知识库文档表
CREATE TABLE IF NOT EXISTS knowledge_document (
    id SERIAL PRIMARY KEY,
    project_id INTEGER,
    user_id INTEGER,
    file_name VARCHAR(255),
    title VARCHAR(255),
    content TEXT,
    doc_type VARCHAR(50),
    file_path VARCHAR(500),
    vector_id VARCHAR(255),
    created_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES user_info(id) ON DELETE SET NULL
);

-- 知识库文档表索引
CREATE INDEX IF NOT EXISTS idx_knowledge_document_project_id ON knowledge_document(project_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_document_user_id ON knowledge_document(user_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_document_created_at ON knowledge_document(created_at DESC);
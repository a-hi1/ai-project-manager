-- =============================================
-- 修复角色和用户数据
-- 添加缺失的 product 和 designer 角色，以及对应的测试账号
-- =============================================

-- 插入缺失的角色（如果不存在）
INSERT INTO role (name, description) VALUES
('product', 'Product Manager'),
('designer', 'UI Designer')
ON CONFLICT (name) DO NOTHING;

-- 查看当前的角色ID（方便确认）
SELECT id, name, description FROM role ORDER BY id;

-- 插入缺失的权限（如果不存在）
INSERT INTO permission (name, description) VALUES
('document:manage', 'Document Management'),
('risk:manage', 'Risk Management'),
('bug:manage', 'Bug Management'),
('change:manage', 'Change Management'),
('ai:use', 'Use AI Features')
ON CONFLICT (name) DO NOTHING;

-- 为新角色分配权限（基于文档描述）
-- Product Manager (应该是 id=6)
INSERT INTO role_permission (role_id, permission_id)
SELECT 
    (SELECT id FROM role WHERE name = 'product'),
    id 
FROM permission 
WHERE name IN ('project:create', 'report:view', 'document:manage', 'ai:use')
ON CONFLICT DO NOTHING;

-- UI Designer (应该是 id=7)
INSERT INTO role_permission (role_id, permission_id)
SELECT 
    (SELECT id FROM role WHERE name = 'designer'),
    id 
FROM permission 
WHERE name IN ('document:manage', 'ai:use')
ON CONFLICT DO NOTHING;

-- 插入缺失的测试账号（如果不存在，密码都是123456）
INSERT INTO user_info (username, password, email, role_id, status) VALUES
('product', '123456', 'product@example.com', (SELECT id FROM role WHERE name = 'product'), 1),
('designer', '123456', 'designer@example.com', (SELECT id FROM role WHERE name = 'designer'), 1)
ON CONFLICT (username) DO NOTHING;

-- 查看修复后的用户列表
SELECT id, username, email, role_id, status FROM user_info ORDER BY id;

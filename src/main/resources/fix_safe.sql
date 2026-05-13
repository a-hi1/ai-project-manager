-- =============================================
-- Fix roles and users (English only - no encoding issues)
-- =============================================

DO $$
BEGIN
    INSERT INTO role (name, description) VALUES
    ('admin', 'System Administrator'),
    ('pm', 'Project Manager'),
    ('developer', 'Developer'),
    ('tester', 'Tester'),
    ('product', 'Product Manager'),
    ('designer', 'UI Designer'),
    ('guest', 'Guest')
    ON CONFLICT (name) DO NOTHING;
END $$;

SELECT id, name, description FROM role ORDER BY id;

DO $$
DECLARE
    v_admin_id INTEGER;
    v_pm_id INTEGER;
    v_developer_id INTEGER;
    v_tester_id INTEGER;
    v_product_id INTEGER;
    v_designer_id INTEGER;
    v_guest_id INTEGER;
BEGIN
    SELECT id INTO v_admin_id FROM role WHERE name = 'admin';
    SELECT id INTO v_pm_id FROM role WHERE name = 'pm';
    SELECT id INTO v_developer_id FROM role WHERE name = 'developer';
    SELECT id INTO v_tester_id FROM role WHERE name = 'tester';
    SELECT id INTO v_product_id FROM role WHERE name = 'product';
    SELECT id INTO v_designer_id FROM role WHERE name = 'designer';
    SELECT id INTO v_guest_id FROM role WHERE name = 'guest';

    INSERT INTO user_info (username, password, email, role_id, status) VALUES
    ('admin', '123456', 'admin@example.com', v_admin_id, 1),
    ('pm', '123456', 'pm@example.com', v_pm_id, 1),
    ('developer', '123456', 'developer@example.com', v_developer_id, 1),
    ('tester', '123456', 'tester@example.com', v_tester_id, 1),
    ('product', '123456', 'product@example.com', v_product_id, 1),
    ('designer', '123456', 'designer@example.com', v_designer_id, 1),
    ('guest', '123456', 'guest@example.com', v_guest_id, 1)
    ON CONFLICT (username) DO UPDATE SET
        password = EXCLUDED.password,
        role_id = EXCLUDED.role_id,
        status = EXCLUDED.status;
END $$;

SELECT 
    u.id,
    u.username,
    u.email,
    u.role_id,
    r.name as role_name,
    u.status
FROM user_info u
LEFT JOIN role r ON u.role_id = r.id
ORDER BY u.id;

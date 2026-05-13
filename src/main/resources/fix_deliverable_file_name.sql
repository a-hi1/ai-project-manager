-- =============================================
-- 修复 deliverable 表 - 添加缺失的 file_name 字段
-- =============================================

-- 检查并添加 file_name 字段
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliverable'
        AND column_name = 'file_name'
    ) THEN
        ALTER TABLE deliverable ADD COLUMN file_name VARCHAR(500);
        RAISE NOTICE '已添加 file_name 字段到 deliverable 表';
    ELSE
        RAISE NOTICE 'file_name 字段已存在';
    END IF;
END $$;

-- 验证修改
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'deliverable'
ORDER BY ordinal_position;

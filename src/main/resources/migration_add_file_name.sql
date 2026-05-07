-- 为 deliverable 表添加 file_name 字段
-- 如果表中没有该字段才添加
ALTER TABLE deliverable ADD COLUMN IF NOT EXISTS file_name VARCHAR(500);

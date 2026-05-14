-- =============================================
-- Fix deliverable table - add missing file_name field
-- =============================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'deliverable'
        AND column_name = 'file_name'
    ) THEN
        ALTER TABLE deliverable ADD COLUMN file_name VARCHAR(500);
        RAISE NOTICE 'Added file_name column to deliverable table';
    ELSE
        RAISE NOTICE 'file_name column already exists';
    END IF;
END $$;

-- Verify the change
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'deliverable'
ORDER BY ordinal_position;

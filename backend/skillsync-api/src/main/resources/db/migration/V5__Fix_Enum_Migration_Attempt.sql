-- This migration ensures columns are properly converted to enum types
-- It checks current state and applies conversion only if needed

-- Check and convert experience_level column if it's still varchar
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'experience_level'
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE users ALTER COLUMN experience_level TYPE experience_level USING experience_level::experience_level;
    END IF;
END
$$;

-- Check and convert current_role column if it's still varchar
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'current_role'
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE users ALTER COLUMN "current_role" TYPE job_role USING "current_role"::job_role;
    END IF;
END
$$;

-- Check and convert target_role column if it's still varchar
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'target_role'
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE users ALTER COLUMN "target_role" TYPE job_role USING "target_role"::job_role;
    END IF;
END
$$;

-- Create indexes if they don't exist
CREATE INDEX IF NOT EXISTS idx_users_current_role ON users("current_role");
CREATE INDEX IF NOT EXISTS idx_users_target_role ON users("target_role");
CREATE INDEX IF NOT EXISTS idx_users_experience_level ON users(experience_level);

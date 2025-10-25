-- This migration converts the PostgreSQL native enum columns back to VARCHAR
-- to work properly with Hibernate's @Enumerated(EnumType.STRING) annotation
-- JSON deserialization converts display names to enum constants automatically

-- Convert current_role column from enum type to VARCHAR
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'current_role'
        AND data_type = 'USER-DEFINED'
    ) THEN
        ALTER TABLE users ALTER COLUMN "current_role" TYPE VARCHAR(255) USING "current_role"::text;
    END IF;
END
$$;

-- Convert target_role column from enum type to VARCHAR
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'target_role'
        AND data_type = 'USER-DEFINED'
    ) THEN
        ALTER TABLE users ALTER COLUMN "target_role" TYPE VARCHAR(255) USING "target_role"::text;
    END IF;
END
$$;

-- Convert experience_level column from enum type to VARCHAR
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'experience_level'
        AND data_type = 'USER-DEFINED'
    ) THEN
        ALTER TABLE users ALTER COLUMN experience_level TYPE VARCHAR(255) USING experience_level::text;
    END IF;
END
$$;

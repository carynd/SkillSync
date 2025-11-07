-- Create experience_level enum type in PostgreSQL (if not exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'experience_level') THEN
        CREATE TYPE experience_level AS ENUM (
            'BEGINNER',
            'INTERMEDIATE',
            'ADVANCED',
            'EXPERT'
        );
    END IF;
END
$$;

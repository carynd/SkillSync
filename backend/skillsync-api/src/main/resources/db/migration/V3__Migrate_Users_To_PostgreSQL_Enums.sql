-- Migrate users table to use PostgreSQL native enum types

-- Step 1: Alter experience_level column to use the experience_level enum type
ALTER TABLE users
ALTER COLUMN experience_level TYPE experience_level
USING experience_level::text::experience_level;

-- Step 2: Alter current_role column to use the job_role enum type
ALTER TABLE users
ALTER COLUMN "current_role" TYPE job_role
USING "current_role"::text::job_role;

-- Step 3: Alter target_role column to use the job_role enum type
ALTER TABLE users
ALTER COLUMN "target_role" TYPE job_role
USING "target_role"::text::job_role;

-- Create index on role columns for better query performance
CREATE INDEX idx_users_current_role ON users("current_role");
CREATE INDEX idx_users_target_role ON users("target_role");
CREATE INDEX idx_users_experience_level ON users(experience_level);

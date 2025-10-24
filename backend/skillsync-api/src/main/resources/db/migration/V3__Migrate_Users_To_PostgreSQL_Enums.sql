-- Migrate users table to use PostgreSQL native enum types

-- Step 1: Alter experience_level column to use the experience_level enum type
-- Handle NULL values and invalid data gracefully
ALTER TABLE users
ALTER COLUMN experience_level TYPE experience_level
USING CASE
    WHEN experience_level IS NULL THEN NULL::experience_level
    WHEN experience_level ~ '^[A-Z_]+$' THEN experience_level::experience_level
    ELSE NULL::experience_level
END;

-- Step 2: Alter current_role column to use the job_role enum type
-- Handle NULL values and invalid data gracefully
ALTER TABLE users
ALTER COLUMN "current_role" TYPE job_role
USING CASE
    WHEN "current_role" IS NULL THEN NULL::job_role
    WHEN "current_role" IN ('Frontend Engineer', 'Backend Engineer', 'Full Stack Engineer', 'DevOps Engineer', 'Data Scientist', 'Machine Learning Engineer', 'Cloud Architect', 'Mobile Developer', 'QA Engineer', 'Solutions Architect', 'Product Manager', 'UX/UI Designer', 'Product Designer', 'Interaction Designer', 'Tech Lead', 'Engineering Manager', 'CTO', 'VP Engineering', 'Project Manager', 'Data Engineer', 'Analytics Engineer', 'Business Analyst', 'Data Analyst', 'Security Engineer', 'Cybersecurity Analyst', 'Infrastructure Engineer', 'Database Administrator', 'AI Engineer', 'Prompt Engineer', 'LLM Engineer', 'Technical Writer', 'Developer Advocate', 'Solutions Engineer', 'Systems Engineer', 'IT Specialist')
    THEN ("current_role"::text || '')::job_role
    ELSE NULL::job_role
END;

-- Step 3: Alter target_role column to use the job_role enum type
-- Handle NULL values and invalid data gracefully
ALTER TABLE users
ALTER COLUMN "target_role" TYPE job_role
USING CASE
    WHEN "target_role" IS NULL THEN NULL::job_role
    WHEN "target_role" IN ('Frontend Engineer', 'Backend Engineer', 'Full Stack Engineer', 'DevOps Engineer', 'Data Scientist', 'Machine Learning Engineer', 'Cloud Architect', 'Mobile Developer', 'QA Engineer', 'Solutions Architect', 'Product Manager', 'UX/UI Designer', 'Product Designer', 'Interaction Designer', 'Tech Lead', 'Engineering Manager', 'CTO', 'VP Engineering', 'Project Manager', 'Data Engineer', 'Analytics Engineer', 'Business Analyst', 'Data Analyst', 'Security Engineer', 'Cybersecurity Analyst', 'Infrastructure Engineer', 'Database Administrator', 'AI Engineer', 'Prompt Engineer', 'LLM Engineer', 'Technical Writer', 'Developer Advocate', 'Solutions Engineer', 'Systems Engineer', 'IT Specialist')
    THEN ("target_role"::text || '')::job_role
    ELSE NULL::job_role
END;

-- Create indexes on enum columns for better query performance
CREATE INDEX IF NOT EXISTS idx_users_current_role ON users("current_role");
CREATE INDEX IF NOT EXISTS idx_users_target_role ON users("target_role");
CREATE INDEX IF NOT EXISTS idx_users_experience_level ON users(experience_level);

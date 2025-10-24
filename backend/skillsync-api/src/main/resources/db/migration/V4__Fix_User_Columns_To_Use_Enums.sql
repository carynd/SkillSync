-- Fix users table columns to properly use PostgreSQL enum types
-- This migration converts VARCHAR columns to actual enum types

-- First, set any invalid or NULL current_role values to NULL
UPDATE users
SET "current_role" = NULL
WHERE "current_role" NOT IN (
    'FRONTEND_ENGINEER', 'BACKEND_ENGINEER', 'FULL_STACK_ENGINEER', 'DEVOPS_ENGINEER',
    'DATA_SCIENTIST', 'MACHINE_LEARNING_ENGINEER', 'CLOUD_ARCHITECT', 'MOBILE_DEVELOPER',
    'QA_ENGINEER', 'SOLUTIONS_ARCHITECT', 'PRODUCT_MANAGER', 'UX_UI_DESIGNER',
    'PRODUCT_DESIGNER', 'INTERACTION_DESIGNER', 'TECH_LEAD', 'ENGINEERING_MANAGER',
    'CTO', 'VP_ENGINEERING', 'PROJECT_MANAGER', 'DATA_ENGINEER', 'ANALYTICS_ENGINEER',
    'BUSINESS_ANALYST', 'DATA_ANALYST', 'SECURITY_ENGINEER', 'CYBERSECURITY_ANALYST',
    'INFRASTRUCTURE_ENGINEER', 'DATABASE_ADMINISTRATOR', 'AI_ENGINEER', 'PROMPT_ENGINEER',
    'LLM_ENGINEER', 'TECHNICAL_WRITER', 'DEVELOPER_ADVOCATE', 'SOLUTIONS_ENGINEER',
    'SYSTEMS_ENGINEER', 'IT_SPECIALIST'
) AND "current_role" IS NOT NULL;

-- Second, set any invalid or NULL target_role values to NULL
UPDATE users
SET "target_role" = NULL
WHERE "target_role" NOT IN (
    'FRONTEND_ENGINEER', 'BACKEND_ENGINEER', 'FULL_STACK_ENGINEER', 'DEVOPS_ENGINEER',
    'DATA_SCIENTIST', 'MACHINE_LEARNING_ENGINEER', 'CLOUD_ARCHITECT', 'MOBILE_DEVELOPER',
    'QA_ENGINEER', 'SOLUTIONS_ARCHITECT', 'PRODUCT_MANAGER', 'UX_UI_DESIGNER',
    'PRODUCT_DESIGNER', 'INTERACTION_DESIGNER', 'TECH_LEAD', 'ENGINEERING_MANAGER',
    'CTO', 'VP_ENGINEERING', 'PROJECT_MANAGER', 'DATA_ENGINEER', 'ANALYTICS_ENGINEER',
    'BUSINESS_ANALYST', 'DATA_ANALYST', 'SECURITY_ENGINEER', 'CYBERSECURITY_ANALYST',
    'INFRASTRUCTURE_ENGINEER', 'DATABASE_ADMINISTRATOR', 'AI_ENGINEER', 'PROMPT_ENGINEER',
    'LLM_ENGINEER', 'TECHNICAL_WRITER', 'DEVELOPER_ADVOCATE', 'SOLUTIONS_ENGINEER',
    'SYSTEMS_ENGINEER', 'IT_SPECIALIST'
) AND "target_role" IS NOT NULL;

-- Third, set any invalid or NULL experience_level values to NULL
UPDATE users
SET experience_level = NULL
WHERE experience_level NOT IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')
AND experience_level IS NOT NULL;

-- Now convert current_role column to job_role enum type
ALTER TABLE users
ALTER COLUMN "current_role" TYPE job_role USING "current_role"::job_role;

-- Convert target_role column to job_role enum type
ALTER TABLE users
ALTER COLUMN "target_role" TYPE job_role USING "target_role"::job_role;

-- Convert experience_level column to experience_level enum type
ALTER TABLE users
ALTER COLUMN experience_level TYPE experience_level USING experience_level::experience_level;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_current_role ON users("current_role");
CREATE INDEX IF NOT EXISTS idx_users_target_role ON users("target_role");
CREATE INDEX IF NOT EXISTS idx_users_experience_level ON users(experience_level);

-- Create job_role enum type in PostgreSQL with all 35+ job roles
CREATE TYPE job_role AS ENUM (
    -- Tech Industry
    'FRONTEND_ENGINEER',
    'BACKEND_ENGINEER',
    'FULL_STACK_ENGINEER',
    'DEVOPS_ENGINEER',
    'DATA_SCIENTIST',
    'MACHINE_LEARNING_ENGINEER',
    'CLOUD_ARCHITECT',
    'MOBILE_DEVELOPER',
    'QA_ENGINEER',
    'SOLUTIONS_ARCHITECT',

    -- Product & Design
    'PRODUCT_MANAGER',
    'UX_UI_DESIGNER',
    'PRODUCT_DESIGNER',
    'INTERACTION_DESIGNER',

    -- Management & Leadership
    'TECH_LEAD',
    'ENGINEERING_MANAGER',
    'CTO',
    'VP_ENGINEERING',
    'PROJECT_MANAGER',

    -- Data & Analytics
    'DATA_ENGINEER',
    'ANALYTICS_ENGINEER',
    'BUSINESS_ANALYST',
    'DATA_ANALYST',

    -- Security & Infrastructure
    'SECURITY_ENGINEER',
    'CYBERSECURITY_ANALYST',
    'INFRASTRUCTURE_ENGINEER',
    'DATABASE_ADMINISTRATOR',

    -- AI & Emerging Tech
    'AI_ENGINEER',
    'PROMPT_ENGINEER',
    'LLM_ENGINEER',

    -- Non-Tech but Tech-Relevant
    'TECHNICAL_WRITER',
    'DEVELOPER_ADVOCATE',
    'SOLUTIONS_ENGINEER',
    'SYSTEMS_ENGINEER',
    'IT_SPECIALIST'
);

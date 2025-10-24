package com.skillsync.enums;

/**
 * Enum representing all available job roles in the SkilSync system.
 * Organized by career category for better structure and maintainability.
 */
public enum JobRole {
    // Tech Industry
    FRONTEND_ENGINEER("Frontend Engineer"),
    BACKEND_ENGINEER("Backend Engineer"),
    FULL_STACK_ENGINEER("Full Stack Engineer"),
    DEVOPS_ENGINEER("DevOps Engineer"),
    DATA_SCIENTIST("Data Scientist"),
    MACHINE_LEARNING_ENGINEER("Machine Learning Engineer"),
    CLOUD_ARCHITECT("Cloud Architect"),
    MOBILE_DEVELOPER("Mobile Developer"),
    QA_ENGINEER("QA Engineer"),
    SOLUTIONS_ARCHITECT("Solutions Architect"),

    // Product & Design
    PRODUCT_MANAGER("Product Manager"),
    UX_UI_DESIGNER("UX/UI Designer"),
    PRODUCT_DESIGNER("Product Designer"),
    INTERACTION_DESIGNER("Interaction Designer"),

    // Management & Leadership
    TECH_LEAD("Tech Lead"),
    ENGINEERING_MANAGER("Engineering Manager"),
    CTO("CTO"),
    VP_ENGINEERING("VP Engineering"),
    PROJECT_MANAGER("Project Manager"),

    // Data & Analytics
    DATA_ENGINEER("Data Engineer"),
    ANALYTICS_ENGINEER("Analytics Engineer"),
    BUSINESS_ANALYST("Business Analyst"),
    DATA_ANALYST("Data Analyst"),

    // Security & Infrastructure
    SECURITY_ENGINEER("Security Engineer"),
    CYBERSECURITY_ANALYST("Cybersecurity Analyst"),
    INFRASTRUCTURE_ENGINEER("Infrastructure Engineer"),
    DATABASE_ADMINISTRATOR("Database Administrator"),

    // AI & Emerging Tech
    AI_ENGINEER("AI Engineer"),
    PROMPT_ENGINEER("Prompt Engineer"),
    LLM_ENGINEER("LLM Engineer"),

    // Non-Tech but Tech-Relevant
    TECHNICAL_WRITER("Technical Writer"),
    DEVELOPER_ADVOCATE("Developer Advocate"),
    SOLUTIONS_ENGINEER("Solutions Engineer"),
    SYSTEMS_ENGINEER("Systems Engineer"),
    IT_SPECIALIST("IT Specialist");

    private final String displayName;

    JobRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the human-readable display name for this job role.
     * @return the display name (e.g., "Frontend Engineer" for FRONTEND_ENGINEER)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Find a JobRole by its display name (case-insensitive).
     * @param displayName the display name to search for
     * @return the matching JobRole, or null if not found
     */
    public static JobRole fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (JobRole role : JobRole.values()) {
            if (role.displayName.equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        return null;
    }

    /**
     * Get all job roles as display names (strings).
     * @return array of all display names sorted alphabetically
     */
    public static String[] getAllDisplayNames() {
        return java.util.Arrays.stream(JobRole.values())
            .map(JobRole::getDisplayName)
            .sorted()
            .toArray(String[]::new);
    }
}

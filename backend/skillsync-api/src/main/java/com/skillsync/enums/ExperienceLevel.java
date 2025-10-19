package com.skillsync.enums;

public enum ExperienceLevel {
    BEGINNER("0-2 years"),
    INTERMEDIATE("3-5 years"),
    ADVANCED("6-10 years"),
    EXPERT("10+ years");

    private final String description;

    ExperienceLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

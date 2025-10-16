package com.skillsync.service;

import com.skillsync.dto.JobPostingResponse;
import com.skillsync.model.SkillDemand;

import java.util.List;

/**
 * Interface for fetching and analyzing job market data.
 * Implementations can use real APIs or mock data.
 */
public interface JobDataService {

    /**
     * Fetch and analyze jobs for a given role
     * @param role The job role to analyze
     * @param location Optional location filter
     * @param maxResults Maximum number of jobs to analyze
     * @return Job analysis with skill demand data
     */
    JobPostingResponse analyzeJobMarket(String role, String location, Integer maxResults);

    /**
     * Get top skills for a specific role
     * @param role The job role
     * @return List of skills with demand scores
     */
    List<SkillDemand> getTopSkillsForRole(String role);

    /**
     * Get list of all available roles in the system
     * @return List of role names
     */
    List<String> getAvailableRoles();
}

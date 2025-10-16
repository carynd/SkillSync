package com.skillsync.controller;

import com.skillsync.dto.JobPostingResponse;
import com.skillsync.dto.JobSyncRequest;
import com.skillsync.model.SkillDemand;
import com.skillsync.service.JobDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Job Intelligence", description = "APIs for analyzing job market and skill demands")
@Slf4j
public class JobController {

    @Autowired
    private JobDataService jobDataService;

    @GetMapping("/{role}/skills")
    @Operation(summary = "Get top skills for a role",
               description = "Returns the most in-demand skills for a specific job role with demand scores")
    public ResponseEntity<List<SkillDemand>> getSkillsForRole(@PathVariable String role) {
        log.info("📊 Getting skills for role: {}", role);
        List<SkillDemand> skills = jobDataService.getTopSkillsForRole(role);
        return ResponseEntity.ok(skills);
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync job market data",
               description = "Fetches latest job postings and updates skill demand index")
    public ResponseEntity<JobPostingResponse> syncJobData(@RequestBody JobSyncRequest request) {
        log.info("🔄 Syncing job data for role: {}", request.getRole());

        String role = request.getRole();
        String location = request.getLocation() != null ? request.getLocation() : "Remote";
        Integer maxResults = request.getMaxResults() != null ? request.getMaxResults() : 100;

        JobPostingResponse response = jobDataService.analyzeJobMarket(role, location, maxResults);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles")
    @Operation(summary = "Get available roles",
               description = "Returns list of all job roles available in the system")
    public ResponseEntity<List<String>> getAvailableRoles() {
        log.info("📋 Getting available roles");
        List<String> roles = jobDataService.getAvailableRoles();
        return ResponseEntity.ok(roles);
    }
}

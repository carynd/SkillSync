package com.skillsync.controller;

import com.skillsync.enums.JobRole;
import com.skillsync.repository.SkillDemandRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin operations for managing job roles and data")
@Slf4j
public class AdminController {

    @Autowired
    private SkillDemandRepository skillDemandRepository;

    private static final List<String> VALID_ROLES = Arrays.stream(JobRole.values())
            .map(JobRole::getDisplayName)
            .toList();

    @PostMapping("/cleanup-roles")
    @Operation(summary = "Clean up invalid roles from database",
               description = "Removes invalid role entries and normalizes role names")
    public ResponseEntity<?> cleanupInvalidRoles() {
        log.info("🧹 Starting cleanup of invalid roles...");

        // Get all distinct roles from database
        List<String> allRoles = skillDemandRepository.findAllDistinctRoles();
        log.info("Found {} distinct roles", allRoles.size());
        log.info("Roles: {}", allRoles);

        // Find invalid roles (not in VALID_ROLES list)
        List<String> invalidRoles = new ArrayList<>();
        for (String role : allRoles) {
            if (!VALID_ROLES.contains(role) && !role.trim().isEmpty()) {
                invalidRoles.add(role);
            }
        }

        log.info("Found {} invalid roles: {}", invalidRoles.size(), invalidRoles);

        // Delete all skill demands for invalid roles
        int deletedCount = 0;
        for (String invalidRole : invalidRoles) {
            try {
                List<com.skillsync.model.SkillDemand> skillsToDelete =
                    skillDemandRepository.findByRoleOrderByDemandScoreDesc(invalidRole);
                skillDemandRepository.deleteAll(skillsToDelete);
                deletedCount += skillsToDelete.size();
                log.info("Deleted {} skill entries for role: '{}'", skillsToDelete.size(), invalidRole);
            } catch (Exception e) {
                log.error("Error deleting skills for role '{}': {}", invalidRole, e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
            "message", "Cleanup completed",
            "invalidRolesFound", invalidRoles.size(),
            "skillsDeleted", deletedCount,
            "invalidRoles", invalidRoles
        ));
    }

    @GetMapping("/valid-roles")
    @Operation(summary = "Get list of valid roles",
               description = "Returns the comprehensive list of valid job roles")
    public ResponseEntity<List<String>> getValidRoles() {
        return ResponseEntity.ok(VALID_ROLES);
    }

    @GetMapping("/roles-status")
    @Operation(summary = "Get status of roles in database",
               description = "Shows which roles are valid, which are invalid, and current database state")
    public ResponseEntity<?> getRolesStatus() {
        List<String> dbRoles = skillDemandRepository.findAllDistinctRoles();

        List<String> validRolesInDb = new ArrayList<>();
        List<String> invalidRolesInDb = new ArrayList<>();

        for (String role : dbRoles) {
            if (VALID_ROLES.contains(role)) {
                validRolesInDb.add(role);
            } else {
                invalidRolesInDb.add(role);
            }
        }

        List<String> missingFromDb = new ArrayList<>(VALID_ROLES);
        missingFromDb.removeAll(validRolesInDb);

        return ResponseEntity.ok(Map.of(
            "totalValidRoles", VALID_ROLES.size(),
            "validRolesInDb", validRolesInDb,
            "validRolesCount", validRolesInDb.size(),
            "invalidRolesInDb", invalidRolesInDb,
            "invalidRolesCount", invalidRolesInDb.size(),
            "missingFromDb", missingFromDb,
            "missingCount", missingFromDb.size()
        ));
    }
}

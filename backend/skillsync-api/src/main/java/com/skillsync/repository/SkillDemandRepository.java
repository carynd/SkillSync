package com.skillsync.repository;

import com.skillsync.model.SkillDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillDemandRepository extends JpaRepository<SkillDemand, UUID> {

    List<SkillDemand> findByRoleOrderByDemandScoreDesc(String role);

    Optional<SkillDemand> findByRoleAndSkill(String role, String skill);

    @Query("SELECT sd FROM SkillDemand sd WHERE sd.role = :role ORDER BY sd.demandScore DESC")
    List<SkillDemand> findTopSkillsByRole(@Param("role") String role);

    @Query("SELECT DISTINCT sd.role FROM SkillDemand sd")
    List<String> findAllDistinctRoles();
}

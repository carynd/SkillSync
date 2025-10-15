package com.skillsync.repository;

import com.skillsync.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {

    Optional<Recommendation> findByUserIdAndExpiresAtAfter(UUID userId, LocalDateTime now);

    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    Optional<Recommendation> findLatestByUserId(@Param("userId") UUID userId);

    void deleteByExpiresAtBefore(LocalDateTime now);
}

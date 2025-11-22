package org.example.interactionservice.repository;

import org.example.interactionservice.entity.Reel;
import org.example.interactionservice.enums.ReelVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReelRepository extends JpaRepository<Reel, Long> {

  @Query("""
      SELECT r FROM Reel r
       WHERE r.userId = :ownerId
         AND r.isDeleted = FALSE
         AND r.expiresAt > :now
       ORDER BY r.createdAt DESC
    """)
  List<Reel> findAliveByOwner(Long ownerId, LocalDateTime now);

  @Query("""
      SELECT r FROM Reel r
       WHERE r.isDeleted = FALSE
         AND r.expiresAt > :now
         AND r.visibilityType = :visibilityType
       ORDER BY r.createdAt DESC
    """)
  List<Reel> findAliveByVisibilityType(ReelVisibility visibilityType, LocalDateTime now);

  @Query("""
      SELECT r FROM Reel r
       WHERE r.id IN :ids
         AND r.isDeleted = FALSE
         AND r.expiresAt > :now
    """)
  List<Reel> findAliveInIds(List<Long> ids, LocalDateTime now);

  @Query("""
      SELECT r FROM Reel r
       WHERE r.isDeleted = FALSE
         AND r.expiresAt <= :now
    """)
  List<Reel> findExpired(LocalDateTime now);
}
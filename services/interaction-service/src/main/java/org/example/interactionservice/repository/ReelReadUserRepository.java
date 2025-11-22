package org.example.interactionservice.repository;

import org.example.interactionservice.entity.ReelReadUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReelReadUserRepository extends JpaRepository<ReelReadUser, Long> {

  @Query("SELECT rru FROM ReelReadUser rru WHERE rru.reel.id = :reelId AND rru.userId = :userId")
  List<ReelReadUser> findByReelIdAndUserId(Long reelId, Long userId);

  @Query("SELECT rru.reel.id FROM ReelReadUser rru WHERE rru.userId = :userId AND rru.reel.id IN :reelIds")
  List<Long> findReadReelIdsIn(Long userId, List<Long> reelIds);

  // NEW: batch fetch all read markers for given reel IDs
  @Query("""
      SELECT rru.reel.id, rru.userId
      FROM ReelReadUser rru
      WHERE rru.reel.id IN :reelIds
    """)
  List<Object[]> findAllReadUsersByReelIds(List<Long> reelIds);
}
package org.example.interactionservice.repository;

import org.example.interactionservice.entity.ReelAllowedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReelAllowedUserRepository extends JpaRepository<ReelAllowedUser, Long> {

  @Query("SELECT r.allowedUserId FROM ReelAllowedUser r WHERE r.reel.id = :reelId")
  List<Long> findAllowedUserIds(Long reelId);

  @Modifying
  @Query("DELETE FROM ReelAllowedUser r WHERE r.reel.id = :reelId")
  void deleteByReelId(Long reelId);
}
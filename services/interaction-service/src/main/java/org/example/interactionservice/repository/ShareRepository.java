package org.example.interactionservice.repository;

import org.example.interactionservice.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {
  int deleteShareByIdAndUserId(Long id, Long userId);
}

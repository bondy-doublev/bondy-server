package org.example.interactionservice.repository;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
  boolean existsByUserIdAndPost(Long userId, Post post);

  @Transactional
  void deleteByUserIdAndPost(Long userId, Post post);

  @Query(value = """
    SELECT r FROM Reaction r
    WHERE r.isNotified = false
    ORDER BY r.id
    LIMIT 500
    """)
  List<Reaction> findUnnotifiedBatch();

  @Transactional
  @Modifying
  @Query("UPDATE Reaction r SET r.isNotified = true WHERE r.id IN :ids")
  void markAsNotified(List<Long> ids);
}

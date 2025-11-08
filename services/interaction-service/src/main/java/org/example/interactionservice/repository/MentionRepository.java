package org.example.interactionservice.repository;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.Mention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentionRepository extends JpaRepository<Mention, Long> {
  @Query(value = """
    SELECT m FROM Mention m
    WHERE m.isNotified = false
    ORDER BY m.id
    LIMIT :limit
    """)
  List<Mention> findUnnotifiedBatch(@Param("limit") int limit);

  @Transactional
  @Modifying
  @Query("UPDATE Mention m SET m.isNotified = true WHERE m.id IN :ids")
  void markAsNotified(List<Long> ids);
}

package org.example.interactionservice.repository;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.MediaAttachment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, Long> {
  @Query("""
    SELECT m
    FROM MediaAttachment m
    JOIN m.post p
    WHERE p.userId = :userId
      AND p.visibility = true
    ORDER BY m.createdAt DESC
    """)
  List<MediaAttachment> findTopByUserId(@Param("userId") Long userId, Pageable pageable);

  @Modifying
  @Transactional
  @Query("""
      DELETE FROM MediaAttachment m
      WHERE m.id = :id
        AND m.post.userId = :userId
    """)
  int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}

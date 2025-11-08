package org.example.interactionservice.repository;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.Comment;
import org.example.interactionservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query("""
      SELECT c FROM Comment c
      WHERE c.post = :post
        AND (:parentId IS NULL AND c.parent IS NULL
             OR :parentId IS NOT NULL AND c.parent.id = :parentId)
    """)
  Page<Comment> findComments(
    @Param("post") Post post,
    @Param("parentId") Long parentId,
    Pageable pageable
  );

  Optional<Comment> findByIdAndPost(Long id, Post post);

  Optional<Comment> findCommentByIdAndUserId(Long id, Long userId);

  @Modifying
  @Transactional
  @Query(value = """
      WITH deleted AS (
        DELETE FROM comments WHERE id = :id AND user_id = :userId RETURNING parent_id, post_id, child_count
      )
      UPDATE posts
      SET comment_count = comment_count - (1 + (SELECT child_count FROM deleted))
      WHERE id IN (SELECT post_id FROM deleted)
    """, nativeQuery = true)
  void decrementPostCommentCount(@Param("id") Long id, @Param("userId") Long userId);

  @Modifying
  @Transactional
  @Query(value = """
      UPDATE comments
      SET child_count = child_count - 1
      WHERE id = (
        SELECT parent_id FROM comments WHERE id = :id
      )
    """, nativeQuery = true)
  void decrementParentChildCount(@Param("id") Long id);

  @Query(value = """
    SELECT c FROM Comment c
    WHERE c.isNotified = false
    ORDER BY c.id
    LIMIT :limit
    """)
  List<Comment> findUnnotifiedBatch(@Param("limit") int limit);

  @Transactional
  @Modifying
  @Query("UPDATE Comment c SET c.isNotified = true WHERE c.id IN :ids")
  void markAsNotified(List<Long> ids);
}

package org.example.interactionservice.repository;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  Page<Post> findByUserId(Long userId, Pageable pageable);

  @Transactional
  @Modifying
  @Query("UPDATE Post p SET p.reactionCount = p.reactionCount + :delta WHERE p.id = :postId")
  void updateReactionCount(@Param("postId") Long postId, @Param("delta") int delta);

  @Transactional
  @Modifying
  @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id = :postId")
  void updateCommentCount(@Param("postId") Long postId, @Param("delta") int delta);

  @Transactional
  @Modifying
  @Query("UPDATE Post p SET p.shareCount = p.shareCount + :delta WHERE p.id = :postId")
  void updateShareCount(@Param("postId") Long postId, @Param("delta") int delta);

  @Query("""
  SELECT p
  FROM Post p
  LEFT JOIN p.postReadUsers r WITH r.userId = :userId
  WHERE p.visibility = true
  GROUP BY p
  ORDER BY 
    CASE WHEN MIN(r.id) IS NULL THEN 0 ELSE 1 END,
    p.createdAt DESC
  """)
  Page<Post> findPublicFeed(@Param("userId") Long userId, Pageable pageable);

  // NEW: wall feed của 1 user, có xét owner/visibility
  @Query("""
    SELECT p FROM Post p
    WHERE p.userId = :userId
      AND (:isOwner = TRUE OR p.visibility = TRUE)
    ORDER BY p.createdAt DESC
    """)
  Page<Post> findWallPosts(@Param("userId") Long userId,
                           @Param("isOwner") Boolean isOwner,
                           Pageable pageable);
}


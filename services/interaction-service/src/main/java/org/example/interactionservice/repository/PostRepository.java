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
}

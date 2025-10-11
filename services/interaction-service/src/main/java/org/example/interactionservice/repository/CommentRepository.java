package org.example.interactionservice.repository;

import org.example.interactionservice.entity.Comment;
import org.example.interactionservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

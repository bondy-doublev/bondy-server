package org.example.interactionservice.repository;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
  boolean existsByUserIdAndPost(Long userId, Post post);

  @Transactional
  void deleteByUserIdAndPost(Long userId, Post post);
}

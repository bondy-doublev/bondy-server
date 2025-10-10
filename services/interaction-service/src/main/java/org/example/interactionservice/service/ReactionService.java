package org.example.interactionservice.service;

import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.dto.response.ReactionResponse;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.entity.Reaction;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.repository.ReactionRepository;
import org.example.interactionservice.service.interfaces.IReactionService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactionService implements IReactionService {
  ReactionRepository reactionRepo;
  PostRepository postRepo;
  EntityManager entityManager;

  @Override
  public ReactionResponse reaction(Long userId, Long postId) {
    Post postRef = entityManager.getReference(Post.class, postId);

    if (reactionRepo.existsByUserIdAndPost(userId, postRef)) {
      reactionRepo.deleteByUserIdAndPost(userId, postRef);
      postRepo.updateReactionCount(postId, -1);
      return null;
    }

    Reaction saved = Reaction.builder()
      .userId(userId)
      .post(postRef)
      .build();

    reactionRepo.save(saved);
    postRepo.updateReactionCount(postId, 1);

    return ReactionResponse.builder()
      .id(saved.getId())
      .userId(saved.getUserId())
      .postId(saved.getPost().getId())
      .createdAt(saved.getCreatedAt())
      .build();
  }
}

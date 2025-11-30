package org.example.interactionservice.service;

import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.client.RecommendationClient;
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
  RecommendationClient recommendationClient;

  @Override
  public ReactionResponse reaction(Long userId, Long postId) {
    Post postRef = entityManager.getReference(Post.class, postId);

    // Nếu đã tồn tại reaction -> remove
    if (reactionRepo.existsByUserIdAndPost(userId, postRef)) {
      reactionRepo.deleteByUserIdAndPost(userId, postRef);
      postRepo.updateReactionCount(postId, -1);

      // Tùy chọn: nếu muốn cập nhật ngược profile khi un-react
      // Có thể gọi triggerRefit() nếu cần rebuild toàn bộ theo chu kỳ ngắn
      // recommendationClient.triggerRefit();

      return null;
    }

    // Tạo reaction mới
    Reaction saved = Reaction.builder()
      .userId(userId)
      .post(postRef)
      .build();

    reactionRepo.save(saved);
    postRepo.updateReactionCount(postId, 1);

    // Gọi qua Recommendation Server để cập nhật profile user
    // best-effort: không chặn luồng chính nếu Recommendation Server lỗi
    recommendationClient.pushUserReact(userId, postId);

    return ReactionResponse.builder()
      .id(saved.getId())
      .userId(saved.getUserId())
      .postId(saved.getPost().getId())
      .createdAt(saved.getCreatedAt())
      .build();
  }
}
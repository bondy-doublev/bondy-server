package org.example.interactionservice.service;

import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.dto.request.CreateCommentRequest;
import org.example.interactionservice.dto.response.CommentResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Comment;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.repository.CommentRepository;
import org.example.interactionservice.service.interfaces.ICommentService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService implements ICommentService {
  EntityManager entityManager;
  CommentRepository commentRepo;

  AuthClient authClient;

  @Override
  public CommentResponse createComment(Long userId, CreateCommentRequest request) {
    Post postRef = entityManager.getReference(Post.class, request.getPostId());
    Comment parent = null;

    if (request.getParentId() != null) {
      parent = entityManager.getReference(Comment.class, request.getParentId());
    }

    Comment newComment = Comment.builder()
      .userId(userId)
      .post(postRef)
      .parent(parent)
      .contentText(request.getContent())
      .build();

    commentRepo.save(newComment);

    UserBasicResponse user = authClient.getBasicProfile(userId);

    return CommentResponse.builder()
      .id(newComment.getId())
      .user(user)
      .parentId(parent != null ? parent.getId() : null)
      .contentText(newComment.getContentText())
      .createdAt(newComment.getCreatedAt())
      .updatedAt(newComment.getUpdatedAt())
      .build();
  }
}

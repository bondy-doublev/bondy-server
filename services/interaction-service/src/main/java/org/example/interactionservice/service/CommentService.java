package org.example.interactionservice.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.dto.request.CreateCommentRequest;
import org.example.interactionservice.dto.response.CommentResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Comment;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.enums.CommentLevel;
import org.example.interactionservice.mapper.CommentMapper;
import org.example.interactionservice.repository.CommentRepository;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.service.interfaces.ICommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService implements ICommentService {
  EntityManager entityManager;
  CommentRepository commentRepo;
  PostRepository postRepo;

  AuthClient authClient;

  @Override
  public CommentResponse createComment(Long userId, Long postId, CreateCommentRequest request) {
    Post postRef = entityManager.getReference(Post.class, postId);

    Comment parent = null;
    CommentLevel level = CommentLevel.LEVEL1;

    if (request.getParentId() != null) {
      Comment parentComment = commentRepo.findByIdAndPost(request.getParentId(), postRef)
        .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Parent comment not found"));

      if (parentComment.getLevel() == CommentLevel.LEVEL2.getValue()) {
        parent = parentComment.getParent();
      } else if (parentComment.getLevel() == CommentLevel.LEVEL1.getValue()) {
        parent = parentComment;
      }

      level = CommentLevel.LEVEL2;
    }

    List<Comment> updates = new ArrayList<>();

    Comment newComment = Comment.builder()
      .userId(userId)
      .post(postRef)
      .parent(parent)
      .level(level.getValue())
      .contentText(request.getContent())
      .build();

    updates.add(newComment);

    if (parent != null) {
      parent.setChildCount(parent.getChildCount() + 1);
      updates.add(parent);
    }

    commentRepo.saveAll(updates);
    postRepo.updateCommentCount(postId, 1);

    UserBasicResponse user = authClient.getBasicProfile(userId);

    return CommentResponse.builder()
      .id(newComment.getId())
      .postId(postId)
      .user(user)
      .parentId(parent != null ? parent.getId() : null)
      .contentText(newComment.getContentText())
      .level(level.getValue())
      .childCount(newComment.getChildCount())
      .createdAt(newComment.getCreatedAt())
      .updatedAt(newComment.getUpdatedAt())
      .build();
  }

  @Override
  public Page<CommentResponse> getPostComments(Long postId, Long parentId, Pageable pageable) {
    Post post = entityManager.getReference(Post.class, postId);

    Page<Comment> comments = commentRepo.findComments(post, parentId, pageable);

    if (comments.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0);
    }

    List<Long> userIds = comments.stream()
      .map(Comment::getUserId)
      .filter(Objects::nonNull)
      .distinct()
      .toList();

    Map<Long, UserBasicResponse> userMap;

    if (!userIds.isEmpty()) {
      List<UserBasicResponse> users = authClient.getBasicProfiles(userIds);
      userMap = users.stream()
        .collect(Collectors.toMap(UserBasicResponse::getId, u -> u));
    } else {
      userMap = Map.of();
    }

    List<CommentResponse> responses = comments.stream()
      .map(c -> CommentMapper.toCommentResponse(c, userMap))
      .toList();

    return new PageImpl<>(responses, pageable, comments.getTotalElements());
  }

  @Override
  @Transactional
  public void deleteComment(Long userId, Long commentId) {
    commentRepo.decrementParentChildCount(commentId);
    commentRepo.decrementPostCommentCount(commentId, userId);
  }
}

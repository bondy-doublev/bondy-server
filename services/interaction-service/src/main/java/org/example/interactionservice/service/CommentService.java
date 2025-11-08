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
import org.example.interactionservice.entity.Mention;
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

import java.util.*;
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

    if (!request.getMentionUserIds().isEmpty()) {
      List<Mention> mentions = new ArrayList<>();

      for (Long id : request.getMentionUserIds()) {
        if (Objects.equals(id, userId)) continue;

        Mention newMention = Mention.builder()
          .comment(newComment)
          .userId(id)
          .build();

        mentions.add(newMention);
      }

      newComment.setMentions(new HashSet<>(mentions));
    }

    updates.add(newComment);

    if (parent != null) {
      parent.setChildCount(parent.getChildCount() + 1);
      updates.add(parent);
    }

    commentRepo.saveAll(updates);
    postRepo.updateCommentCount(postId, 1);

    Set<Long> relatedUserIds = new HashSet<>();
    relatedUserIds.add(newComment.getUserId());

    if (newComment.getMentions() != null && !newComment.getMentions().isEmpty()) {
      relatedUserIds.addAll(
        newComment.getMentions().stream()
          .map(Mention::getUserId)
          .toList()
      );
    }

    Map<Long, UserBasicResponse> userMap;

    if (!relatedUserIds.isEmpty()) {
      List<UserBasicResponse> users = authClient.getBasicProfiles(relatedUserIds.stream().toList());
      userMap = users.stream()
        .collect(Collectors.toMap(UserBasicResponse::getId, u -> u));
    } else {
      userMap = Map.of();
    }

    return CommentMapper.toCommentResponse(newComment, userMap);
  }

  @Override
  public Page<CommentResponse> getPostComments(Long postId, Long parentId, Pageable pageable) {
    Post post = entityManager.getReference(Post.class, postId);

    Page<Comment> comments = commentRepo.findComments(post, parentId, pageable);

    if (comments.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0);
    }

    List<Long> userIds = comments.stream()
      .flatMap(c -> {
        List<Long> ids = new ArrayList<>();
        if (c.getUserId() != null) ids.add(c.getUserId());
        if (c.getMentions() != null) {
          c.getMentions().forEach(m -> ids.add(m.getUserId()));
        }
        return ids.stream();
      })
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

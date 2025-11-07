package org.example.interactionservice.mapper;

import org.example.interactionservice.dto.response.CommentResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Comment;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommentMapper {

  public static CommentResponse toCommentResponse(Comment comment, Map<Long, UserBasicResponse> userMap) {
    List<UserBasicResponse> mentionUsers = comment.getMentions() == null
      ? List.of()
      : comment.getMentions().stream()
      .map(m -> userMap.get(m.getUserId()))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    return CommentResponse.builder()
      .id(comment.getId())
      .postId(comment.getPost().getId())
      .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
      .user(userMap.get(comment.getUserId()))
      .mentions(mentionUsers)
      .contentText(comment.getContentText())
      .level(comment.getLevel())
      .childCount(comment.getChildCount())
      .createdAt(comment.getCreatedAt())
      .updatedAt(comment.getUpdatedAt())
      .build();
  }

}

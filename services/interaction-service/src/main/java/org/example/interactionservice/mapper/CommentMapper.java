package org.example.interactionservice.mapper;

import org.example.interactionservice.dto.response.CommentResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Comment;

import java.util.Map;

public class CommentMapper {

  public static CommentResponse toCommentResponse(Comment comment, Map<Long, UserBasicResponse> userMap) {
    return CommentResponse.builder()
      .id(comment.getId())
      .postId(comment.getPost().getId())
      .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
      .user(userMap.get(comment.getUserId()))
      .contentText(comment.getContentText())
      .level(comment.getLevel())
      .childCount(comment.getChildCount())
      .createdAt(comment.getCreatedAt())
      .updatedAt(comment.getUpdatedAt())
      .build();
  }
}

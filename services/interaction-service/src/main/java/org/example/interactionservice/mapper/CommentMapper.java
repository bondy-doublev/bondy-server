package org.example.interactionservice.mapper;

import org.example.interactionservice.dto.response.CommentResponse;
import org.example.interactionservice.dto.response.ParentCommentResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Comment;

import java.util.Map;

public class CommentMapper {

  public static CommentResponse toCommentResponse(Comment comment, Map<Long, UserBasicResponse> userMap) {
    ParentCommentResponse parentResponse = null;

    if (comment.getParent() != null) {
      Long parentUserId = comment.getParent().getUserId();
      UserBasicResponse parentUser = userMap.get(parentUserId);

      parentResponse = ParentCommentResponse.builder()
        .parentId(comment.getParent().getId())
        .userId(parentUserId)
        .userName(parentUser != null ? parentUser.getFullName() : "Unknown User")
        .build();
    }

    return CommentResponse.builder()
      .id(comment.getId())
      .postId(comment.getPost().getId())
      .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
      .parentComment(parentResponse)
      .user(userMap.get(comment.getUserId()))
      .contentText(comment.getContentText())
      .level(comment.getLevel())
      .childCount(comment.getChildCount())
      .createdAt(comment.getCreatedAt())
      .updatedAt(comment.getUpdatedAt())
      .build();
  }
}

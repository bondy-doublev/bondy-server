package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.request.CreateCommentRequest;
import org.example.interactionservice.dto.response.CommentResponse;

public interface ICommentService {
  CommentResponse createComment(Long userId, CreateCommentRequest request);
}

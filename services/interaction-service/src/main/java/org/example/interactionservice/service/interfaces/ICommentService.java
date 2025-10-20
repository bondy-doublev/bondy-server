package org.example.interactionservice.service.interfaces;

import jakarta.transaction.Transactional;
import org.example.interactionservice.dto.request.CreateCommentRequest;
import org.example.interactionservice.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICommentService {
  CommentResponse createComment(Long userId, Long postId, CreateCommentRequest request);

  Page<CommentResponse> getPostComments(Long postId, Long parentId, Pageable pageable);
  
  @Transactional
  void deleteComment(Long userId, Long commentId);
}

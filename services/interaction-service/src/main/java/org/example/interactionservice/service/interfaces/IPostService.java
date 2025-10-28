package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.dto.response.PostResponse;

public interface IPostService {
  PostResponse createPost(Long ownerId, CreatePostRequest request);

  void deletePost(Long userId, Long postId);
}
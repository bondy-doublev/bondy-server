package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.dto.request.UpdatePostRequest;
import org.example.interactionservice.dto.response.PostResponse;

public interface IPostService {
  PostResponse createPost(Long ownerId, CreatePostRequest request);

  PostResponse updatePost(Long userId, Long postId, UpdatePostRequest request);

  void deletePost(Long userId, Long postId);
}
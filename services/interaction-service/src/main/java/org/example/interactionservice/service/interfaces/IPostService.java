package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPostService {
  Page<PostResponse> getNewFeed(Pageable pageable);

  Page<PostResponse> getWall(Long userId, Pageable pageable);

  PostResponse createPost(Long ownerId, CreatePostRequest request);

  void deletePost(Long userId, Long postId);
}
package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPostService {
    Page<Post> getNewFeed(Pageable pageable);
    Page<Post> getWall(Long userId, Pageable pageable);
    Post createPost(Long ownerId, CreatePostRequest request);
}
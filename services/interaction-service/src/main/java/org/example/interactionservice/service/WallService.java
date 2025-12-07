package org.example.interactionservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.MediaAttachment;
import org.example.interactionservice.entity.Mention;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.repository.MediaAttachmentRepository;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.service.interfaces.IWallService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WallService implements IWallService {

  MediaAttachmentRepository mediaAttachmentRepo;
  PostRepository postRepo;
  AuthClient authClient;

  @Override
  public Page<PostResponse> getWallFeed(Long userId, Pageable pageable) {
    Long currentUserId = ContextUser.get().getUserId();
    boolean isOwner = currentUserId.equals(userId);

    // 1. Lấy tất cả post (bao gồm cả share-post) của user trên wall
    Page<Post> page = postRepo.findWallPosts(userId, isOwner, pageable);
    List<Post> wallPosts = page.getContent();

    if (wallPosts.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, page.getTotalElements());
    }

    // 2. Gom thêm bài gốc của các bài share (sharedFrom)
    Set<Post> allPosts = new HashSet<>(wallPosts);
    for (Post p : wallPosts) {
      if (p.getSharedFrom() != null) {
        allPosts.add(p.getSharedFrom());
      }
    }

    // 3. Gom tất cả userId cần lấy thông tin (owner + taggedUsers)
    Set<Long> userIds = new HashSet<>();
    for (Post p : allPosts) {
      userIds.add(p.getUserId());
      for (Mention m : p.getTags()) {
        userIds.add(m.getUserId());
      }
    }

    Map<Long, UserBasicResponse> userMap = authClient.getBasicProfiles(new ArrayList<>(userIds))
      .stream()
      .collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

    // 4. Map Post -> PostResponse (có sharedFrom)
    List<PostResponse> responses = wallPosts.stream()
      .map(p -> buildPostResponse(p, userMap, currentUserId))
      .toList();

    return new PageImpl<>(responses, pageable, page.getTotalElements());
  }

  private PostResponse buildPostResponse(
    Post post,
    Map<Long, UserBasicResponse> userMap,
    Long currentUserId
  ) {
    // owner + tagged cho chính post này
    UserBasicResponse owner = userMap.get(post.getUserId());
    List<UserBasicResponse> taggedUsers = post.getTags().stream()
      .map(Mention::getUserId)
      .map(userMap::get)
      .filter(Objects::nonNull)
      .toList();

    boolean reacted = post.getReactions().stream()
      .anyMatch(r -> r.getUserId().equals(currentUserId));

    // nếu là bài share thì build PostResponse cho bài gốc
    PostResponse originalPostResponse = null;
    if (post.getSharedFrom() != null) {
      Post original = post.getSharedFrom();

      UserBasicResponse originalOwner = userMap.get(original.getUserId());
      List<UserBasicResponse> originalTaggedUsers = original.getTags().stream()
        .map(Mention::getUserId)
        .map(userMap::get)
        .filter(Objects::nonNull)
        .toList();

      boolean originalReacted = original.getReactions().stream()
        .anyMatch(r -> r.getUserId().equals(currentUserId));

      originalPostResponse = original.toPostResponse(
        originalOwner,
        originalTaggedUsers,
        originalReacted,
        null // original của original
      );
    }

    // build PostResponse cho post trên wall (gốc hoặc share)
    return post.toPostResponse(
      owner,
      taggedUsers,
      reacted,
      originalPostResponse
    );
  }

  @Override
  public List<MediaAttachment> getWallMedia(Long userId, Pageable pageable) {
    return mediaAttachmentRepo.findTopByUserId(userId, pageable);
  }
}

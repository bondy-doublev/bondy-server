package org.example.interactionservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.response.FeedItemResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.MediaAttachment;
import org.example.interactionservice.entity.Mention;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.enums.FeedType;
import org.example.interactionservice.repository.FeedRepository;
import org.example.interactionservice.repository.MediaAttachmentRepository;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.service.interfaces.IWallService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WallService implements IWallService {
  MediaAttachmentRepository mediaAttachmentRepo;
  FeedRepository feedRepo;
  PostRepository postRepo;

  AuthClient authClient;

  @Override
  public Page<FeedItemResponse> getWallFeed(Long userId, Pageable pageable) {
    Page<Object[]> raw = feedRepo.getWallFeed(userId, pageable);
    Set<Long> userIds = new HashSet<>();
    Set<Long> postIds = new HashSet<>();

    for (Object[] row : raw) {
      userIds.add(((Number) row[2]).longValue());
      if (row[4] != null) {
        postIds.add(((Number) row[4]).longValue());
        userIds.add(((Number) row[5]).longValue());
      } else if ("POST".equals(row[0])) {
        postIds.add(((Number) row[1]).longValue());
      }
    }

    if (userIds.isEmpty() || postIds.isEmpty())
      return null;

    List<Post> posts = postRepo.findAllById(postIds);

    posts.forEach(post -> {
      userIds.add(post.getUserId());
      post.getTags().forEach(tag -> userIds.add(tag.getUserId()));
    });

    Map<Long, UserBasicResponse> userMap = authClient.getBasicProfiles(userIds.stream().toList())
      .stream().collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

    Map<Long, Post> postMap = posts
      .stream().collect(Collectors.toMap(Post::getId, p -> p));

    return raw.map(row -> {
      String type = (String) row[0];
      Long id = ((Number) row[1]).longValue();
      Long userIdRaw = ((Number) row[2]).longValue();
      LocalDateTime createdAt = ((Timestamp) row[3]).toLocalDateTime();
      Long sharedPostId = row[4] != null ? ((Number) row[4]).longValue() : null;

      UserBasicResponse user = userMap.get(userIdRaw);

      if (FeedType.POST.name().equals(type)) {
        Post post = postMap.get(id);
        if (post == null)
          return null;

        List<UserBasicResponse> taggedUsers = post.getTags().stream()
          .map(Mention::getUserId)
          .map(userMap::get)
          .filter(Objects::nonNull)
          .toList();

        return FeedItemResponse.builder()
          .type(FeedType.POST.name())
          .id(id)
          .user(user)
          .post(post.toPostResponse(
            userMap.get(post.getUserId()),
            taggedUsers,
            post.getReactions().stream()
              .anyMatch(r -> r.getUserId().equals(ContextUser.get().getUserId()))
          ))
          .createdAt(createdAt)
          .build();
      } else {
        Post sharedPost = sharedPostId != null ? postMap.get(sharedPostId) : null;

        if (sharedPost == null) {
          return FeedItemResponse.builder()
            .type(FeedType.SHARE.name())
            .id(id)
            .user(user)
            .post(null)
            .createdAt(createdAt)
            .build();
        }
        
        UserBasicResponse sharedOwner = userMap.get(sharedPost.getUserId());

        List<UserBasicResponse> taggedUsers = sharedPost.getTags().stream()
          .map(Mention::getUserId)
          .map(userMap::get)
          .filter(Objects::nonNull)
          .toList();

        return FeedItemResponse.builder()
          .type(FeedType.SHARE.name())
          .id(id)
          .user(user)
          .post(sharedPost.toPostResponse(sharedOwner, taggedUsers, sharedPost.getReactions().stream().anyMatch(r -> r.getUserId().equals(ContextUser.get().getUserId()))))
          .createdAt(createdAt)
          .build();
      }
    });
  }

  @Override
  public List<MediaAttachment> getWallMedia(Long userId) {
    Pageable topNine = PageRequest.of(0, 9);
    return mediaAttachmentRepo.findTopByUserId(userId, topNine);
  }

}

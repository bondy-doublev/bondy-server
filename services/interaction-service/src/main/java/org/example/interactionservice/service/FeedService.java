package org.example.interactionservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.client.RecommendationClient;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.response.FeedItemResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Mention;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.entity.RecommendedPost;
import org.example.interactionservice.repository.FeedRepository;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.service.interfaces.IFeedService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedService implements IFeedService {
  FeedRepository feedRepo;
  PostRepository postRepo;
  AuthClient authClient;
  RecommendationClient recommendationClient;

//  @Override
//  public Page<FeedItemResponse> getFeed(Pageable pageable) {
//    Page<Object[]> raw = feedRepo.getFeed(pageable);
//    Set<Long> userIds = new HashSet<>();
//    Set<Long> postIds = new HashSet<>();
//
//    for (Object[] row : raw) {
//      userIds.add(((Number) row[2]).longValue());
//      if (row[4] != null) {
//        postIds.add(((Number) row[4]).longValue());
//      } else if ("POST".equals(row[0])) {
//        postIds.add(((Number) row[1]).longValue());
//      }
//    }
//
//    if (userIds.isEmpty() || postIds.isEmpty())
//      return null;
//
//    List<Post> posts = postRepo.findAllById(postIds);
//
//    posts.forEach(post -> {
//      userIds.add(post.getUserId());
//      post.getTags().forEach(tag -> userIds.add(tag.getUserId()));
//    });
//
//    Map<Long, UserBasicResponse> userMap = authClient.getBasicProfiles(userIds.stream().toList())
//      .stream().collect(Collectors.toMap(UserBasicResponse::getId, u -> u));
//
//    Map<Long, Post> postMap = posts
//      .stream().collect(Collectors.toMap(Post::getId, p -> p));
//
//    return raw.map(row -> {
//      String type = (String) row[0];
//      Long id = ((Number) row[1]).longValue();
//      Long userId = ((Number) row[2]).longValue();
//      LocalDateTime createdAt = ((Timestamp) row[3]).toLocalDateTime();
//      Long sharedPostId = row[4] != null ? ((Number) row[4]).longValue() : null;
//
//      UserBasicResponse user = userMap.get(userId);
//
//      if (FeedType.POST.name().equals(type)) {
//        Post post = postMap.get(id);
//        if (post == null)
//          return null;
//
//        List<UserBasicResponse> taggedUsers = post.getTags().stream()
//          .map(Mention::getUserId)
//          .map(userMap::get)
//          .filter(Objects::nonNull)
//          .toList();
//
//        return FeedItemResponse.builder()
//          .type(FeedType.POST.name())
//          .id(id)
//          .user(user)
//          .post(post.toPostResponse(
//            userMap.get(post.getUserId()),
//            taggedUsers,
//            post.getReactions().stream()
//              .anyMatch(r -> r.getUserId().equals(ContextUser.get().getUserId()))
//          ))
//          .createdAt(createdAt)
//          .build();
//      } else {
//        Post sharedPost = sharedPostId != null ? postMap.get(sharedPostId) : null;
//        if (sharedPost == null) {
//          return FeedItemResponse.builder()
//            .type(FeedType.SHARE.name())
//            .id(id)
//            .user(user)
//            .post(null)
//            .createdAt(createdAt)
//            .build();
//        }
//
//        UserBasicResponse sharedOwner = userMap.get(sharedPost.getUserId());
//
//        List<UserBasicResponse> taggedUsers = sharedPost.getTags().stream()
//          .map(Mention::getUserId)
//          .map(userMap::get)
//          .filter(Objects::nonNull)
//          .toList();
//
//        return FeedItemResponse.builder()
//          .type(FeedType.SHARE.name())
//          .id(id)
//          .user(user)
//          .post(sharedPost.toPostResponse(sharedOwner, taggedUsers, sharedPost.getReactions().stream().anyMatch(r -> r.getUserId().equals(ContextUser.get().getUserId()))))
//          .createdAt(createdAt)
//          .build();
//      }
//    });
//  }

  @Override
  public Page<FeedItemResponse> getFeed(Pageable pageable) {
    // ============= PART 1: LẤY FEED GỐC =============
    Page<Object[]> raw = feedRepo.getFeed(pageable);

    Set<Long> userIds = new HashSet<>();
    Set<Long> postIds = new HashSet<>();

    for (Object[] row : raw) {
      userIds.add(((Number) row[2]).longValue());
      if (row[4] != null) {
        postIds.add(((Number) row[4]).longValue());
      } else if ("POST".equals(row[0])) {
        postIds.add(((Number) row[1]).longValue());
      }
    }

    if (userIds.isEmpty() || postIds.isEmpty())
      return new PageImpl<>(List.of(), pageable, 0);

    List<Post> posts = postRepo.findAllById(postIds);

    posts.forEach(post -> {
      userIds.add(post.getUserId());
      post.getTags().forEach(tag -> userIds.add(tag.getUserId()));
    });

    Map<Long, UserBasicResponse> userMap = authClient.getBasicProfiles(userIds.stream().toList())
      .stream().collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

    Map<Long, Post> postMap = posts
      .stream().collect(Collectors.toMap(Post::getId, p -> p));

    // ============= PART 2: GỌI PYTHON LẤY RECOMMEND =============
    Long currentUserId = ContextUser.get().getUserId();
    int offset = pageable.getPageNumber() * pageable.getPageSize();
    int limit = pageable.getPageSize();

    // Lấy danh sách recommend đã sort theo score
    List<RecommendedPost> rec = recommendationClient.getRecommendedPosts(currentUserId, offset, limit);

    List<FeedItemResponse> recommendedFeed = new ArrayList<>();
    if (rec != null && !rec.isEmpty()) {
      // Map Post từ database theo id
      Map<Long, Post> recPostMap = postRepo.findAllById(
        rec.stream().map(RecommendedPost::getId).toList()
      ).stream().collect(Collectors.toMap(Post::getId, p -> p));

      // Map user cho owner của các recommend post
      List<Long> recUserIds = recPostMap.values().stream().map(Post::getUserId).toList();
      Map<Long, UserBasicResponse> recUserMap = authClient.getBasicProfiles(recUserIds)
        .stream().collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

      // Build list giữ nguyên thứ tự (score giảm dần)
      for (RecommendedPost r : rec) {
        Post p = recPostMap.get(r.getId());
        if (p == null) continue;

        UserBasicResponse owner = recUserMap.get(p.getUserId());
        FeedItemResponse item = FeedItemResponse.builder()
          .type("POST")
          .id(p.getId())
          .user(owner)
          .post(
            p.toPostResponse(
              owner,
              List.of(),       // tagged users
              false            // isLiked
            )
          )
          .score(r.getScore())             // <--- truyền score đúng từ Python
          .createdAt(p.getCreatedAt())
          .build();

        recommendedFeed.add(item);
      }
    }

    // ============= PART 3: BUILD FEED GỐC NHƯ CŨ =============
    List<FeedItemResponse> originalFeed = raw.stream().map(row -> {
      String type = (String) row[0];
      Long id = ((Number) row[1]).longValue();
      Long userId = ((Number) row[2]).longValue();
      LocalDateTime createdAt = ((Timestamp) row[3]).toLocalDateTime();
      Long sharedPostId = row[4] != null ? ((Number) row[4]).longValue() : null;

      UserBasicResponse user = userMap.get(userId);

      if ("POST".equals(type)) {
        Post post = postMap.get(id);
        if (post == null) return null;

        List<UserBasicResponse> taggedUsers = post.getTags().stream()
          .map(Mention::getUserId)
          .map(userMap::get)
          .filter(Objects::nonNull)
          .toList();

        return FeedItemResponse.builder()
          .type("POST")
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
            .type("SHARE")
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
          .type("SHARE")
          .id(id)
          .user(user)
          .post(sharedPost.toPostResponse(
            sharedOwner,
            taggedUsers,
            sharedPost.getReactions().stream()
              .anyMatch(r -> r.getUserId().equals(ContextUser.get().getUserId()))
          ))
          .createdAt(createdAt)
          .build();
      }
    }).filter(Objects::nonNull).toList();

    // ============= PART 4: GHÉP FEED: rec trên, feed cũ dưới =============
    List<FeedItemResponse> combined = new ArrayList<>();
    combined.addAll(recommendedFeed);   // đã đúng thứ tự score
    combined.addAll(originalFeed);

    return new PageImpl<>(combined, pageable, combined.size());
  }

}

package org.example.interactionservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.client.RecommendationClient;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Mention;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.entity.RecommendedPost;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.service.interfaces.IFeedService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedService implements IFeedService {

  PostRepository postRepo;
  AuthClient authClient;
  RecommendationClient recommendationClient;

  @Override
  public Page<PostResponse> getFeed(Pageable pageable) {
    Long currentUserId = ContextUser.get().getUserId();

    // ============= PART 1: LẤY FEED GỐC TỪ POSTS ============= //
    Page<Post> basePage = postRepo.findPublicFeed(pageable);
    List<Post> basePosts = basePage.getContent();

    // ============= PART 2: LẤY RECOMMEND TỪ PYTHON (OPTIONAL) ============= //
    int offset = pageable.getPageNumber() * pageable.getPageSize();
    int limit = pageable.getPageSize();

    List<RecommendedPost> rec = recommendationClient.getRecommendedPosts(currentUserId, offset, limit);

    List<Post> recommendedPosts = new ArrayList<>();
    if (rec != null && !rec.isEmpty()) {
      // map id -> index để giữ đúng thứ tự score
      Map<Long, Integer> orderMap = new HashMap<>();
      for (int i = 0; i < rec.size(); i++) {
        orderMap.put(rec.get(i).getId(), i);
      }

      List<Long> recIds = rec.stream().map(RecommendedPost::getId).toList();
      recommendedPosts = postRepo.findAllById(recIds);

      // sort theo thứ tự score (dựa theo vị trí trong list rec)
      recommendedPosts.sort(Comparator.comparingInt(p -> orderMap.getOrDefault(p.getId(), Integer.MAX_VALUE)));
    }

    // ============= PART 3: TẬP HỢP TẤT CẢ POST CẦN DÙNG ============= //
    List<Post> allFeedPosts = new ArrayList<>();
    allFeedPosts.addAll(recommendedPosts);
    allFeedPosts.addAll(basePosts);

    // thêm cả bài gốc của các bài share (sharedFrom)
    Set<Post> allPostsWithOriginal = new HashSet<>(allFeedPosts);
    for (Post p : allFeedPosts) {
      if (p.getSharedFrom() != null) {
        allPostsWithOriginal.add(p.getSharedFrom());
      }
    }

    // ============= PART 4: LẤY USER (owner + tagged) BẰNG AuthClient ============= //
    Set<Long> userIds = new HashSet<>();
    for (Post p : allPostsWithOriginal) {
      userIds.add(p.getUserId());
      for (Mention m : p.getTags()) {
        userIds.add(m.getUserId());
      }
    }

    Map<Long, UserBasicResponse> userMap = authClient.getBasicProfiles(new ArrayList<>(userIds))
      .stream()
      .collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

    // ============= PART 5: MAP -> PostResponse (có sharedFrom) ============= //
    List<PostResponse> recommendedResponses = recommendedPosts.stream()
      .map(p -> buildPostResponse(p, userMap, currentUserId))
      .toList();

    List<PostResponse> baseResponses = basePosts.stream()
      .map(p -> buildPostResponse(p, userMap, currentUserId))
      .toList();

    // gộp: rec ở trên, base ở dưới
    List<PostResponse> combined = new ArrayList<>();
    combined.addAll(recommendedResponses);
    combined.addAll(baseResponses);

    // totalElements bạn có thể quyết định:
    // - Nếu muốn chuẩn: dùng basePage.getTotalElements() + rec.size()
    // - Ở đây dùng combined.size() cho đơn giản
    return new PageImpl<>(combined, pageable, combined.size());
  }

  private PostResponse buildPostResponse(
    Post post,
    Map<Long, UserBasicResponse> userMap,
    Long currentUserId
  ) {
    // owner + tagged của chính bài này
    UserBasicResponse owner = userMap.get(post.getUserId());
    List<UserBasicResponse> taggedUsers = post.getTags().stream()
      .map(Mention::getUserId)
      .map(userMap::get)
      .filter(Objects::nonNull)
      .toList();

    boolean reacted = post.getReactions().stream()
      .anyMatch(r -> r.getUserId().equals(currentUserId));

    // nếu là bài share thì build originalPostResponse
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

    // dùng toPostResponse bạn đã định nghĩa
    return post.toPostResponse(
      owner,
      taggedUsers,
      reacted,
      originalPostResponse
    );
  }
}

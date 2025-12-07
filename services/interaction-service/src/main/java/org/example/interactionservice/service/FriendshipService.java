package org.example.interactionservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.dto.response.FriendSuggestResponse;
import org.example.interactionservice.dto.response.FriendshipResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Friendship;
import org.example.interactionservice.repository.FriendshipRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendshipService {

  FriendshipRepository friendshipRepository;
  AuthClient authClient;

  // =========================
  // Helper: convert List -> Page
  // =========================
  private <T> Page<T> toPage(List<T> list, Pageable pageable) {
    if (list == null) {
      list = List.of();
    }

    // ✅ Nếu unpaged hoặc null → trả hết, không cắt
    if (pageable == null || pageable.isUnpaged()) {
      return new PageImpl<>(list);
    }

    int total = list.size();
    int start = (int) pageable.getOffset();
    if (start >= total) {
      return new PageImpl<>(List.of(), pageable, total);
    }
    int end = Math.min(start + pageable.getPageSize(), total);
    List<T> content = list.subList(start, end);

    return new PageImpl<>(content, pageable, total);
  }

  // =========================
  // Friend request actions
  // =========================

  // Gửi request kết bạn
  public Friendship sendFriendRequest(Long senderId, Long receiverId) {
    if (friendshipRepository.findByUserIdAndFriendId(senderId, receiverId).isPresent()) {
      throw new RuntimeException("Friend request already exists");
    }

    Friendship f = Friendship.builder()
      .userId(senderId)
      .friendId(receiverId)
      .status(Friendship.Status.PENDING)
      .requestedAt(LocalDateTime.now())
      .build();

    return friendshipRepository.save(f);
  }

  // Chấp nhận request
  public Friendship acceptFriendRequest(Long receiverId, Long senderId) {
    Friendship f = friendshipRepository.findByUserIdAndFriendId(senderId, receiverId)
      .orElseThrow(() -> new RuntimeException("Friend request not found"));

    f.setStatus(Friendship.Status.ACCEPTED);
    f.setRespondedAt(LocalDateTime.now());

    authClient.updateFriendCount(senderId, receiverId, "add");

    return friendshipRepository.save(f);
  }

  // Từ chối request
  public void rejectFriendRequest(Long receiverId, Long senderId) {
    Friendship f = friendshipRepository.findByUserIdAndFriendId(senderId, receiverId)
      .orElseThrow(() -> new RuntimeException("Friend request not found"));
    f.setStatus(Friendship.Status.REJECTED);
    f.setRespondedAt(LocalDateTime.now());
    friendshipRepository.save(f);
  }

  // =========================
  // Internal helpers for friendships
  // =========================

  /**
   * Lấy toàn bộ friends (đã ACCEPTED) của user, trả List<FriendshipResponse>
   * (không phân trang – dùng nội bộ).
   */
  private List<FriendshipResponse> getAllFriendsInternal(Long userId) {
    // 1. Lấy tất cả friendships đã accept
    List<Friendship> fromUser =
      friendshipRepository.findByUserIdAndStatus(userId, Friendship.Status.ACCEPTED);
    List<Friendship> toUser =
      friendshipRepository.findByFriendIdAndStatus(userId, Friendship.Status.ACCEPTED);

    List<Friendship> allFriends = new ArrayList<>();
    allFriends.addAll(fromUser);
    allFriends.addAll(toUser);

    if (allFriends.isEmpty()) {
      return List.of();
    }

    // 2. Lấy tất cả userId liên quan (sender + receiver)
    List<Long> userIds = allFriends.stream()
      .flatMap(f -> Stream.of(f.getUserId(), f.getFriendId()))
      .distinct()
      .toList();

    // 3. Lấy thông tin user từ AuthClient
    List<UserBasicResponse> users = authClient.getBasicProfiles(userIds);

    // 4. Map userId -> user info
    Map<Long, UserBasicResponse> userMap = users.stream()
      .collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

    // 5. Chuyển sang DTO trả về
    return allFriends.stream()
      .map(f -> {
        UserBasicResponse sender = userMap.get(f.getUserId());
        UserBasicResponse receiver = userMap.get(f.getFriendId());
        return new FriendshipResponse(f, sender, receiver);
      })
      .toList();
  }

  // =========================
  // Public APIs (paged)
  // =========================

  // Get accepted friends of a user (paged hoặc unpaged)
  public Page<FriendshipResponse> getFriends(Long userId, Pageable pageable) {
    List<FriendshipResponse> all = getAllFriendsInternal(userId);
    return toPage(all, pageable); // ✅ unpaged đã được handle ở toPage
  }

  public Page<FriendshipResponse> getPendingRequests(Long userId, Pageable pageable) {
    // 1. Lấy tất cả pending requests
    List<Friendship> friendships =
      friendshipRepository.findByFriendIdAndStatus(userId, Friendship.Status.PENDING);

    if (friendships.isEmpty()) {
      return toPage(List.of(), pageable);
    }

    // 2. Lấy tất cả userId liên quan (senderId + receiverId)
    List<Long> userIds = friendships.stream()
      .flatMap(f -> Stream.of(f.getUserId(), f.getFriendId()))
      .distinct()
      .toList();

    // 3. Lấy thông tin user từ service/client
    List<UserBasicResponse> users = authClient.getBasicProfiles(userIds);

    // 4. Map userId -> user info
    Map<Long, UserBasicResponse> userMap = users.stream()
      .collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

    // 5. Chuyển sang DTO trả về
    List<FriendshipResponse> responses = friendships.stream()
      .map(f -> {
        UserBasicResponse sender = userMap.get(f.getUserId());
        UserBasicResponse receiver = userMap.get(f.getFriendId());
        return new FriendshipResponse(f, sender, receiver);
      })
      .toList();

    return toPage(responses, pageable);
  }

  public List<FriendSuggestResponse> suggestFriends(Long userId, int page) {
    // 1️⃣ Lấy danh sách bạn bè (1 độ) dạng Set cho O(1)
    Set<Long> myFriends = getFriendIds(userId);

    // 2️⃣ Pending mình gửi đi
    Set<Long> pendingSent = friendshipRepository
      .findByUserIdAndStatus(userId, Friendship.Status.PENDING)
      .stream()
      .map(Friendship::getFriendId)
      .collect(Collectors.toSet());

    // 3️⃣ Pending người khác gửi tới mình
    Set<Long> pendingReceived = friendshipRepository
      .findByFriendIdAndStatus(userId, Friendship.Status.PENDING)
      .stream()
      .map(Friendship::getUserId)
      .collect(Collectors.toSet());

    // 4️⃣ Lấy user từ auth-service theo page
    List<UserBasicResponse> allUsers = authClient.getAllBasicProfiles(page);
    if (allUsers.isEmpty()) {
      return List.of();
    }

    // 5️⃣ Lọc: không phải mình, không phải bạn, không pending 2 chiều
    List<UserBasicResponse> notFriends = allUsers.stream()
      .filter(u -> {
        Long id = u.getId();
        return !id.equals(userId)
          && !myFriends.contains(id)
          && !pendingSent.contains(id)
          && !pendingReceived.contains(id);
      })
      .toList();

    if (notFriends.isEmpty()) {
      return List.of();
    }

    // 6️⃣ Tập id candidate để lookup mutual
    Set<Long> candidateIds = notFriends.stream()
      .map(UserBasicResponse::getId)
      .collect(Collectors.toSet());

    // Map: candidateId -> mutualCount
    Map<Long, Long> mutualCounts = new HashMap<>();

    if (!myFriends.isEmpty() && !candidateIds.isEmpty()) {
      // 7️⃣ Lấy tất cả friendships ACCEPTED mà 1 đầu là bạn của mình
      List<Friendship> secondDegreeFriendships =
        friendshipRepository.findAllByStatusAndUserIdInOrFriendIdIn(
          Friendship.Status.ACCEPTED,
          myFriends
        );

      // 8️⃣ Duyệt, count mutual:
      // (friend của mình) <-> (candidate)
      for (Friendship f : secondDegreeFriendships) {
        Long u1 = f.getUserId();
        Long u2 = f.getFriendId();

        // Nếu u1 là bạn mình, u2 là candidate
        if (myFriends.contains(u1) && candidateIds.contains(u2)) {
          mutualCounts.merge(u2, 1L, Long::sum);
        }

        // Nếu u2 là bạn mình, u1 là candidate
        if (myFriends.contains(u2) && candidateIds.contains(u1)) {
          mutualCounts.merge(u1, 1L, Long::sum);
        }
      }
    }

    // 9️⃣ Map ra response + sort theo mutual ↓ rồi nearBy
    return notFriends.stream()
      .map(u -> {
        long mutual = mutualCounts.getOrDefault(u.getId(), 0L);
        boolean nearBy = u.getAddress() != null && !u.getAddress().isBlank();
        return new FriendSuggestResponse(u, mutual, nearBy, false);
      })
      .sorted((a, b) -> {
        int cmpMutual = Long.compare(b.getMutualFriends(), a.getMutualFriends());
        if (cmpMutual != 0) return cmpMutual;
        return Boolean.compare(b.getNearBy(), a.getNearBy());
      })
      // .limit(100) // nếu bạn muốn cắt tối đa 100 gợi ý thì thêm dòng này
      .toList();
  }

  public Page<FriendshipResponse> getPendingSentRequests(Long userId, Pageable pageable) {
    // Lấy tất cả friendship mà user gửi đi và chưa được accept
    List<Friendship> pending =
      friendshipRepository.findByUserIdAndStatus(userId, Friendship.Status.PENDING);

    if (pending.isEmpty()) {
      return toPage(List.of(), pageable);
    }

    // Lấy tất cả userId liên quan
    List<Long> userIds = pending.stream()
      .flatMap(f -> Stream.of(f.getUserId(), f.getFriendId()))
      .distinct()
      .toList();

    // Lấy info user từ AuthClient
    List<UserBasicResponse> users = authClient.getBasicProfiles(userIds);
    Map<Long, UserBasicResponse> userMap = users.stream()
      .collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

    // Map sang DTO
    List<FriendshipResponse> responses = pending.stream()
      .map(f -> new FriendshipResponse(
        f,
        userMap.get(f.getUserId()),
        userMap.get(f.getFriendId())
      ))
      .toList();

    return toPage(responses, pageable);
  }

  // =========================
  // Status + Unfriend
  // =========================

  public FriendshipResponse getFriendshipStatus(Long contextUserId, Long userId) {
    if (contextUserId.equals(userId)) {
      return null;
    }

    Friendship f = friendshipRepository
      .findByUserIdAndFriendId(contextUserId, userId)
      .orElseGet(() -> friendshipRepository
        .findByUserIdAndFriendId(userId, contextUserId)
        .orElse(null)
      );

    if (f == null) {
      return null;
    }

    return new FriendshipResponse(f, null, null);
  }

  public void unFriend(Long contextUserId, Long userId) {
    if (contextUserId.equals(userId)) {
      throw new AppException(ErrorCode.BAD_REQUEST, "Cannot unfriend yourself");
    }

    Friendship friendship = friendshipRepository
      .findBetweenUsers(contextUserId, userId)
      .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Friendship not found"));

    friendshipRepository.delete(friendship);
  }

  private Set<Long> getFriendIds(Long userId) {
    // friendships mà mình là user_id
    List<Friendship> fromUser =
      friendshipRepository.findByUserIdAndStatus(userId, Friendship.Status.ACCEPTED);

    // friendships mà mình là friend_id
    List<Friendship> toUser =
      friendshipRepository.findByFriendIdAndStatus(userId, Friendship.Status.ACCEPTED);

    Set<Long> result = new HashSet<>();

    for (Friendship f : fromUser) {
      result.add(f.getFriendId());
    }
    for (Friendship f : toUser) {
      result.add(f.getUserId());
    }
    return result;
  }

}

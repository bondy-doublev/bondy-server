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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendshipService {

  FriendshipRepository friendshipRepository;
  AuthClient authClient;

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

  public List<FriendshipResponse> getFriends(Long userId) {
    // 1. Lấy tất cả friendships đã accept
    List<Friendship> fromUser = friendshipRepository.findByUserIdAndStatus(userId, Friendship.Status.ACCEPTED);
    List<Friendship> toUser = friendshipRepository.findByFriendIdAndStatus(userId, Friendship.Status.ACCEPTED);

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
    List<FriendshipResponse> responses = allFriends.stream()
      .map(f -> {
        UserBasicResponse sender = userMap.get(f.getUserId());
        UserBasicResponse receiver = userMap.get(f.getFriendId());
        return new FriendshipResponse(f, sender, receiver);
      })
      .toList();

    return responses;
  }


  public List<FriendshipResponse> getPendingRequests(Long userId) {
    // 1. Lấy tất cả pending requests
    List<Friendship> friendships = friendshipRepository.findByFriendIdAndStatus(userId, Friendship.Status.PENDING);

    if (friendships.isEmpty()) {
      return List.of();
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

    return responses;
  }

  public List<FriendSuggestResponse> suggestFriends(Long userId, int page) {
    // 1️⃣ Lấy danh sách bạn bè hiện tại
    List<Long> myFriends = getFriends(userId).stream()
      .map(f -> f.getSenderId().equals(userId) ? f.getReceiverId() : f.getSenderId())
      .toList();

    // 2️⃣ Lấy danh sách pending request gửi từ user
    List<Long> pendingSent = friendshipRepository
      .findByUserIdAndStatus(userId, Friendship.Status.PENDING)
      .stream()
      .map(Friendship::getFriendId)
      .toList();

    // 3️⃣ Lấy danh sách pending request gửi tới user (chưa accept)
    List<Long> pendingReceived = friendshipRepository
      .findByFriendIdAndStatus(userId, Friendship.Status.PENDING)
      .stream()
      .map(Friendship::getUserId)
      .toList();

    // 4️⃣ Lấy user từ authClient theo trang
    List<UserBasicResponse> allUsers = authClient.getAllBasicProfiles(page);

    // 5️⃣ Lọc những người chưa phải bạn, chưa gửi request, và chưa gửi tới mình
    List<UserBasicResponse> notFriends = allUsers.stream()
      .filter(u -> !u.getId().equals(userId)
        && !myFriends.contains(u.getId())
        && !pendingSent.contains(u.getId())
        && !pendingReceived.contains(u.getId()) // ✅ loại bỏ những user đang pending gửi tới mình
      )
      .toList();

    // 6️⃣ Nếu <100 thì trả hết luôn
    if (notFriends.size() < 100) {
      return notFriends.stream()
        .map(u -> new FriendSuggestResponse(
          u,
          0L,
          u.getAddress() != null && !u.getAddress().isBlank(),
          false // không cần check pendingReceived nữa vì đã filter ở trên
        ))
        .toList();
    }

    // 7️⃣ Nếu >=100 thì tính mutualFriends + nearBy và sắp xếp
    return notFriends.stream()
      .map(u -> {
        long mutualFriends = myFriends.stream()
          .filter(fId -> getFriends(fId).stream()
            .anyMatch(f -> f.getSenderId().equals(u.getId()) || f.getReceiverId().equals(u.getId()))
          ).count();
        boolean nearBy = u.getAddress() != null && !u.getAddress().isBlank();
        return new FriendSuggestResponse(u, mutualFriends, nearBy, false);
      })
      .sorted((a, b) -> {
        if (!b.getMutualFriends().equals(a.getMutualFriends())) {
          return Long.compare(b.getMutualFriends(), a.getMutualFriends());
        }
        return Boolean.compare(b.getNearBy(), a.getNearBy());
      })
      .toList();
  }


  public List<FriendshipResponse> getPendingSentRequests(Long userId) {
    // Lấy tất cả friendship mà user gửi đi và chưa được accept
    List<Friendship> pending = friendshipRepository.findByUserIdAndStatus(userId, Friendship.Status.PENDING);

    if (pending.isEmpty()) return List.of();

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
    return pending.stream()
      .map(f -> new FriendshipResponse(
        f,
        userMap.get(f.getUserId()),
        userMap.get(f.getFriendId())
      ))
      .toList();
  }

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

    // Tuỳ business, nhưng thường:
    // - ACCEPTED  -> unfriend
    // - PENDING   -> hủy lời mời kết bạn
    // - REJECTED  -> coi như không có quan hệ, xóa luôn cũng được
    friendshipRepository.delete(friendship);
  }
}

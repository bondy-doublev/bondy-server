package org.example.interactionservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.dto.request.CreateReelRequest;
import org.example.interactionservice.dto.request.UpdateReelVisibilityRequest;
import org.example.interactionservice.dto.response.FriendshipResponse;
import org.example.interactionservice.dto.response.ReelResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Reel;
import org.example.interactionservice.entity.ReelAllowedUser;
import org.example.interactionservice.enums.ReelVisibility;
import org.example.interactionservice.repository.ReelAllowedUserRepository;
import org.example.interactionservice.repository.ReelRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReelService {

  private final ReelRepository reelRepository;
  private final ReelAllowedUserRepository reelAllowedUserRepository;
  private final AuthClient authClient;
  private final FriendshipService friendshipService;

  private static final int DEFAULT_TTL_HOURS = 24;

  @Transactional
  public ReelResponse createReel(CreateReelRequest request) {
    validateCreateRequest(request);

    LocalDateTime expiresAt = LocalDateTime.now()
      .plusHours(request.getTtlHours() != null ? request.getTtlHours() : DEFAULT_TTL_HOURS);

    Reel reel = Reel.builder()
      .userId(request.getUserId())
      .videoUrl(request.getVideoUrl())
      .visibilityType(request.getVisibilityType())
      .expiresAt(expiresAt)
      .viewCount(0L)
      .isDeleted(false)
      .isNotified(false)
      .build();

    Reel saved = reelRepository.save(reel);

    // Nếu CUSTOM -> insert mapping
    if (request.getVisibilityType() == ReelVisibility.CUSTOM && request.getCustomAllowedUserIds() != null) {
      List<ReelAllowedUser> entities = request.getCustomAllowedUserIds().stream()
        .distinct()
        .map(uid -> ReelAllowedUser.builder()
          .reel(saved)
          .allowedUserId(uid)
          .build())
        .toList();
      reelAllowedUserRepository.saveAll(entities);
      saved.setAllowedUsers(new HashSet<>(entities));
    }

    UserBasicResponse owner = authClient.getBasicProfile(saved.getUserId());
    return saved.toResponse(owner, true);
  }

  @Transactional
  public void deleteReel(Long reelId, Long requesterId) {
    Reel reel = reelRepository.findById(reelId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Reel not found"));
    if (!Objects.equals(reel.getUserId(), requesterId)) {
      throw new AppException(ErrorCode.UNAUTHORIZED, "You are not allowed to delete this Reel");
    }
    reel.setIsDeleted(true);
    reelRepository.save(reel);
  }

  @Transactional
  public ReelResponse updateVisibility(UpdateReelVisibilityRequest request) {
    Reel reel = reelRepository.findById(request.getReelId())
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Reel not found"));

    if (!Objects.equals(reel.getUserId(), request.getRequesterId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED, "You are not allowed to update this Reel");
    }

    reel.setVisibilityType(request.getVisibilityType());

    // Clear old custom list
    reelAllowedUserRepository.deleteByReelId(reel.getId());
    reel.getAllowedUsers().clear();

    if (request.getVisibilityType() == ReelVisibility.CUSTOM) {
      List<Long> customIds = Optional.ofNullable(request.getCustomAllowedUserIds()).orElse(List.of());
      List<ReelAllowedUser> newItems = customIds.stream()
        .distinct()
        .map(uid -> ReelAllowedUser.builder()
          .reel(reel)
          .allowedUserId(uid)
          .build())
        .toList();
      reelAllowedUserRepository.saveAll(newItems);
      reel.setAllowedUsers(new HashSet<>(newItems));
    }

    reelRepository.save(reel);

    UserBasicResponse owner = authClient.getBasicProfile(reel.getUserId());
    return reel.toResponse(owner, true);
  }

  /**
   * Lấy danh sách Reel mà viewer có thể thấy (của tất cả bạn bè hoặc của một user cụ thể).
   * Nếu ownerId != null -> chỉ lấy reel của user đó nếu viewer có quyền.
   * Nếu ownerId == null -> lấy reels của toàn bộ friends của viewer.
   */
  @Transactional()
  public List<ReelResponse> getVisibleReels(Long viewerId, Long ownerId) {
    LocalDateTime now = LocalDateTime.now();

    // 1. Lấy danh sách bạn bè của viewer
    List<FriendshipResponse> friendships = friendshipService.getFriends(viewerId);
    Set<Long> friendIds = friendships.stream()
      .flatMap(f -> Set.of(f.getSenderId(), f.getReceiverId()).stream())
      .filter(id -> !id.equals(viewerId))
      .collect(Collectors.toSet());

    // 2. Xác định tập owners mục tiêu
    Set<Long> targetOwners = new HashSet<>();
    if (ownerId != null) {
      if (Objects.equals(ownerId, viewerId) || friendIds.contains(ownerId)) {
        targetOwners.add(ownerId);
      } else {
        return List.of(); // Không phải bạn cũng không phải chính mình
      }
    } else {
      targetOwners.addAll(friendIds);
      targetOwners.add(viewerId); // để thấy PRIVATE của mình
    }

    if (targetOwners.isEmpty()) {
      return List.of();
    }

    // 3. Lấy reels sống của toàn bộ owners (có thể thêm method mới trong repository)
    // Nếu chưa có method findAliveByOwnerIn thì tạm loop như cũ:
    List<Reel> reels = new ArrayList<>();
    for (Long oId : targetOwners) {
      reels.addAll(reelRepository.findAliveByOwner(oId, now));
    }

    // 4. Filter visibility
    List<Reel> visibleReels = reels.stream()
      .filter(r -> !r.getIsDeleted()) // vì query đã đảm bảo expiresAt > now
      .filter(r -> {
        if (Objects.equals(r.getUserId(), viewerId)) {
          return true; // chủ sở hữu luôn thấy
        }
        boolean isFriend = friendIds.contains(r.getUserId());
        return switch (r.getVisibilityType()) {
          case PUBLIC -> isFriend; // theo logic bạn yêu cầu
          case PRIVATE -> false;
          case CUSTOM -> isFriend && r.getAllowedUsers()
            .stream()
            .anyMatch(au -> Objects.equals(au.getAllowedUserId(), viewerId));
        };
      })
      .toList();

    if (visibleReels.isEmpty()) {
      return List.of();
    }

    // 5. Lấy thông tin owner
    Set<Long> ownerIds = visibleReels.stream()
      .map(Reel::getUserId)
      .collect(Collectors.toSet());

    List<UserBasicResponse> owners = authClient.getBasicProfiles(new ArrayList<>(ownerIds));
    Map<Long, UserBasicResponse> ownerMap = owners.stream()
      .collect(Collectors.toMap(UserBasicResponse::getId, u -> u));

    // 6. Map response
    return visibleReels.stream()
      .map(r -> r.toResponse(ownerMap.get(r.getUserId()), true))
      .toList();
  }

  @Transactional
  public void incrementViewCount(Long reelId, Long viewerId) {
    Reel reel = reelRepository.findById(reelId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Reel not found"));
    if (reel.isExpired() || reel.getIsDeleted()) {
      throw new AppException(ErrorCode.ENTITY_NOT_FOUND, "Reel is expired");
    }
    // Optional: kiểm tra viewer có quyền xem trước khi count
    reel.setViewCount(reel.getViewCount() + 1);
    reelRepository.save(reel);
  }

  /**
   * Cleanup hoặc mark reels đã hết hạn (có thể gọi từ scheduler).
   * Ở đây chỉ set isDeleted = true cho đơn giản.
   */
  @Transactional
  public int expireReelsJob() {
    LocalDateTime now = LocalDateTime.now();
    List<Reel> expired = reelRepository.findExpired(now);
    expired.forEach(r -> r.setIsDeleted(true));
    reelRepository.saveAll(expired);
    return expired.size();
  }

  private void validateCreateRequest(CreateReelRequest request) {
    if (request.getUserId() == null) {
      throw new IllegalArgumentException("userId không được null");
    }
    if (request.getVideoUrl() == null || request.getVideoUrl().isBlank()) {
      throw new IllegalArgumentException("videoUrl không được trống");
    }
    if (request.getVisibilityType() == null) {
      throw new IllegalArgumentException("visibilityType không được null");
    }
    if (request.getVisibilityType() == ReelVisibility.CUSTOM &&
      (request.getCustomAllowedUserIds() == null || request.getCustomAllowedUserIds().isEmpty())) {
      throw new IllegalArgumentException("CUSTOM phải có danh sách customAllowedUserIds");
    }
  }
}
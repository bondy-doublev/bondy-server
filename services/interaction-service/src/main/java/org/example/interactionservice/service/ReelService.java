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
import org.example.interactionservice.entity.ReelReadUser;
import org.example.interactionservice.enums.ReelVisibility;
import org.example.interactionservice.repository.ReelAllowedUserRepository;
import org.example.interactionservice.repository.ReelReadUserRepository;
import org.example.interactionservice.repository.ReelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReelService {

  private final ReelRepository reelRepository;
  private final ReelAllowedUserRepository reelAllowedUserRepository;
  private final ReelReadUserRepository reelReadUserRepository;

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
    return saved.toResponse(owner, true, false, List.of());
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
    return reel.toResponse(owner, true, false, List.of());
  }

  @Transactional
  public List<ReelResponse> getVisibleReels(Long viewerId, Long ownerId) {
    LocalDateTime now = LocalDateTime.now();

    Set<Long> friendIds = getFriendIds(viewerId);
    Set<Long> targetOwners = resolveTargetOwners(viewerId, ownerId, friendIds);
    if (targetOwners.isEmpty()) return List.of();

    List<Reel> reels = new ArrayList<>();
    for (Long oId : targetOwners) {
      reels.addAll(reelRepository.findAliveByOwner(oId, now));
    }

    List<Reel> visibleReels = reels.stream()
      .filter(r -> !r.getIsDeleted())
      .filter(r -> canViewReelIgnoringExpiry(r, viewerId, friendIds)) // alive variant also valid
      .toList();

    if (visibleReels.isEmpty()) return List.of();

    Map<Long, UserBasicResponse> ownerMap = loadOwnerProfiles(visibleReels);

    List<Long> reelIds = visibleReels.stream().map(Reel::getId).toList();
    Map<Long, List<Long>> readIdMap = buildReadUserIdMap(reelIds);
    Map<Long, List<UserBasicResponse>> readUsersProfileMap = buildReadUserProfileMap(readIdMap);

    return visibleReels.stream()
      .map(r -> {
        List<UserBasicResponse> readUsers = readUsersProfileMap.getOrDefault(r.getId(), List.of());
        boolean isRead = readUsers.stream().anyMatch(u -> Objects.equals(u.getId(), viewerId));
        return r.toResponse(ownerMap.get(r.getUserId()), true, isRead, readUsers);
      })
      .toList();
  }

  @Transactional
  public List<ReelResponse> getAllReels(Long viewerId, Long ownerId) {
    Set<Long> friendIds = getFriendIds(viewerId);
    Set<Long> targetOwners = resolveTargetOwners(viewerId, ownerId, friendIds);
    if (targetOwners.isEmpty()) return List.of();

    List<Reel> reels = reelRepository.findAllNotDeletedByOwnerIn(new ArrayList<>(targetOwners));
    if (reels.isEmpty()) return List.of();

    List<Reel> visibleReels = reels.stream()
      .filter(r -> canViewReelIgnoringExpiry(r, viewerId, friendIds))
      .toList();

    if (visibleReels.isEmpty()) return List.of();

    Map<Long, UserBasicResponse> ownerMap = loadOwnerProfiles(visibleReels);

    List<Long> reelIds = visibleReels.stream().map(Reel::getId).toList();
    Map<Long, List<Long>> readIdMap = buildReadUserIdMap(reelIds);
    Map<Long, List<UserBasicResponse>> readUsersProfileMap = buildReadUserProfileMap(readIdMap);

    return visibleReels.stream()
      .map(r -> {
        List<UserBasicResponse> readUsers = readUsersProfileMap.getOrDefault(r.getId(), List.of());
        boolean isRead = readUsers.stream().anyMatch(u -> Objects.equals(u.getId(), viewerId));
        return r.toResponse(ownerMap.get(r.getUserId()), true, isRead, readUsers);
      })
      .toList();
  }

  @Transactional
  public void incrementViewCount(Long reelId, Long viewerId) {
    Reel reel = reelRepository.findById(reelId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Reel not found"));
    if (reel.isExpired() || reel.getIsDeleted()) {
      throw new AppException(ErrorCode.ENTITY_NOT_FOUND, "Reel is expired");
    }
    // (Optional) validate viewer can see before counting
    reel.setViewCount(reel.getViewCount() + 1);
    reelRepository.save(reel);
  }

  @Transactional
  public int expireReelsJob() {
    LocalDateTime now = LocalDateTime.now();
    List<Reel> expired = reelRepository.findExpired(now);
    expired.forEach(r -> r.setIsDeleted(true));
    reelRepository.saveAll(expired);
    return expired.size();
  }

  @Transactional
  public void markRead(Long reelId, Long viewerId) {
    Reel reel = reelRepository.findById(reelId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Reel not found"));
    if (reel.getIsDeleted()) {
      throw new AppException(ErrorCode.ENTITY_NOT_FOUND, "Reel deleted");
    }
    if (!canViewReelIgnoringExpiry(reel, viewerId)) {
      throw new AppException(ErrorCode.UNAUTHORIZED, "No permission to read this reel");
    }
    boolean alreadyRead = !reelReadUserRepository.findByReelIdAndUserId(reelId, viewerId).isEmpty();
    if (!alreadyRead) {
      ReelReadUser readUser = ReelReadUser.builder()
        .reel(reel)
        .userId(viewerId)
        .readAt(LocalDateTime.now())
        .build();
      reelReadUserRepository.save(readUser);
    }
  }

  /* ===================== Helpers ===================== */

  private void validateCreateRequest(CreateReelRequest request) {
    if (request.getUserId() == null) {
      throw new IllegalArgumentException("userId must not be null");
    }
    if (request.getVideoUrl() == null || request.getVideoUrl().isBlank()) {
      throw new IllegalArgumentException("videoUrl must not be blank");
    }
    if (request.getVisibilityType() == null) {
      throw new IllegalArgumentException("visibilityType must not be null");
    }
    if (request.getVisibilityType() == ReelVisibility.CUSTOM &&
      (request.getCustomAllowedUserIds() == null || request.getCustomAllowedUserIds().isEmpty())) {
      throw new IllegalArgumentException("CUSTOM visibility requires customAllowedUserIds list");
    }
  }

  private Set<Long> getFriendIds(Long viewerId) {
    Page<FriendshipResponse> friendships = friendshipService.getFriends(viewerId, Pageable.unpaged());
    return friendships.stream()
      .flatMap(f -> Set.of(f.getSenderId(), f.getReceiverId()).stream())
      .filter(id -> !id.equals(viewerId))
      .collect(Collectors.toSet());
  }

  private Set<Long> resolveTargetOwners(Long viewerId, Long ownerId, Set<Long> friendIds) {
    Set<Long> targetOwners = new HashSet<>();
    if (ownerId != null) {
      if (Objects.equals(ownerId, viewerId) || friendIds.contains(ownerId)) {
        targetOwners.add(ownerId);
      }
    } else {
      targetOwners.addAll(friendIds);
      targetOwners.add(viewerId);
    }
    return targetOwners;
  }

  private Map<Long, UserBasicResponse> loadOwnerProfiles(List<Reel> reels) {
    Set<Long> ownerIds = reels.stream().map(Reel::getUserId).collect(Collectors.toSet());
    List<UserBasicResponse> owners = authClient.getBasicProfiles(new ArrayList<>(ownerIds));
    return owners.stream().collect(Collectors.toMap(UserBasicResponse::getId, u -> u));
  }

  private Map<Long, List<Long>> buildReadUserIdMap(List<Long> reelIds) {
    if (reelIds.isEmpty()) return Collections.emptyMap();
    List<Object[]> rows = reelReadUserRepository.findAllReadUsersByReelIds(reelIds);
    Map<Long, List<Long>> map = new HashMap<>();
    for (Object[] row : rows) {
      Long reelId = (Long) row[0];
      Long userId = (Long) row[1];
      map.computeIfAbsent(reelId, k -> new ArrayList<>()).add(userId);
    }
    map.replaceAll((k, v) -> List.copyOf(v));
    return map;
  }

  private Map<Long, List<UserBasicResponse>> buildReadUserProfileMap(Map<Long, List<Long>> readIdMap) {
    Set<Long> allIds = readIdMap.values().stream()
      .flatMap(List::stream)
      .collect(Collectors.toSet());
    if (allIds.isEmpty()) {
      return Collections.emptyMap();
    }
    List<UserBasicResponse> profiles = authClient.getBasicProfiles(new ArrayList<>(allIds));
    Map<Long, UserBasicResponse> profileMap = profiles.stream()
      .collect(Collectors.toMap(UserBasicResponse::getId, p -> p));

    Map<Long, List<UserBasicResponse>> result = new HashMap<>();
    readIdMap.forEach((reelId, userIds) -> {
      List<UserBasicResponse> users = userIds.stream()
        .map(profileMap::get)
        .filter(Objects::nonNull)
        .toList();
      result.put(reelId, users);
    });
    return result;
  }

  // Single method variant with precomputed friendIds
  private boolean canViewReelIgnoringExpiry(Reel reel, Long viewerId, Set<Long> friendIds) {
    if (Objects.equals(reel.getUserId(), viewerId)) return true;
    boolean isFriend = friendIds.contains(reel.getUserId());
    return switch (reel.getVisibilityType()) {
      case PUBLIC -> isFriend;
      case PRIVATE -> false;
      case CUSTOM -> isFriend &&
        reel.getAllowedUsers().stream().anyMatch(au -> Objects.equals(au.getAllowedUserId(), viewerId));
    };
  }

  // Convenience variant when friendIds not precomputed
  private boolean canViewReelIgnoringExpiry(Reel reel, Long viewerId) {
    return canViewReelIgnoringExpiry(reel, viewerId, getFriendIds(viewerId));
  }
}
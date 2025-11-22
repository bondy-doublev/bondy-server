package org.example.interactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.dto.response.ReelResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Base.BaseEntityWithUpdate;
import org.example.interactionservice.enums.ReelVisibility;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "reels")
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reel extends BaseEntityWithUpdate {

  @Column(name = "user_id", nullable = false)
  Long userId;

  @Column(name = "video_url", nullable = false, length = 1024)
  String videoUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility_type", nullable = false, length = 20)
  ReelVisibility visibilityType;

  @Column(name = "expires_at", nullable = false)
  LocalDateTime expiresAt;

  @Column(name = "is_deleted", nullable = false)
  Boolean isDeleted = false;

  @Column(name = "view_count", nullable = false)
  Long viewCount;

  @Column(name = "is_notified", nullable = false)
  Boolean isNotified;

  @Builder.Default
  @OneToMany(mappedBy = "reel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  Set<ReelAllowedUser> allowedUsers = Set.of();

  public ReelResponse toResponse(UserBasicResponse owner, boolean visibleForRequester) {
    return ReelResponse.builder()
      .id(this.getId())
      .videoUrl(this.getVideoUrl())
      .visibilityType(this.getVisibilityType())
      .expiresAt(this.getExpiresAt())
      .viewCount(this.getViewCount())
      .owner(owner)
      .visible(visibleForRequester)
      .customAllowedUserIds(
        this.getAllowedUsers().stream()
          .map(ReelAllowedUser::getAllowedUserId)
          .collect(Collectors.toList())
      )
      .createdAt(this.getCreatedAt())
      .updatedAt(this.getUpdatedAt())
      .build();
  }

  public ReelResponse toResponse(UserBasicResponse owner,
                                 boolean visibleForRequester,
                                 boolean isRead,
                                 java.util.List<UserBasicResponse> readUsers) {
    return ReelResponse.builder()
      .id(this.getId())
      .videoUrl(this.getVideoUrl())
      .visibilityType(this.getVisibilityType())
      .expiresAt(this.getExpiresAt())
      .viewCount(this.getViewCount())
      .owner(owner)
      .visible(visibleForRequester)
      .customAllowedUserIds(
        this.getAllowedUsers().stream()
          .map(ReelAllowedUser::getAllowedUserId)
          .collect(Collectors.toList())
      )
      .createdAt(this.getCreatedAt())
      .updatedAt(this.getUpdatedAt())
      .isRead(isRead)
      .readUsers(readUsers)
      .build();
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiresAt);
  }
}
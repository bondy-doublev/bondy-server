package org.example.interactionservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.dto.response.MediaAttachmentResponse;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Base.BaseEntityWithUpdate;
import org.hibernate.annotations.DynamicInsert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "posts")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntityWithUpdate {
  @Column(name = "user_id")
  Long userId;

  @Column(name = "content_text")
  String contentText;

  @Column(name = "media_count")
  Integer mediaCount;
  Boolean visibility = true;

  @Column(name = "reaction_count")
  Long reactionCount;

  @Column(name = "comment_count")
  Long commentCount;

  @Column(name = "share_count")
  Long shareCount;

  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @JsonManagedReference
  Set<MediaAttachment> mediaAttachments = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @ToString.Exclude
  Set<Reaction> reactions = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @ToString.Exclude
  Set<Share> shares = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @ToString.Exclude
  Set<Comment> comments = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @ToString.Exclude
  Set<Mention> tags = new HashSet<>();

  public PostResponse toPostResponse(UserBasicResponse owner, List<UserBasicResponse> taggedUsers, Boolean reacted) {
    return PostResponse.builder()
      .id(this.getId())
      .contentText(this.getContentText())
      .mediaCount(this.getMediaCount())
      .visibility(this.getVisibility())
      .reactionCount(this.getReactionCount())
      .commentCount(this.getCommentCount())
      .shareCount(this.getShareCount())
      .createdAt(this.getCreatedAt())
      .updatedAt(this.getUpdatedAt())
      .mediaAttachments(
        this.getMediaAttachments()
          .stream()
          .map(media -> MediaAttachmentResponse.builder()
            .id(media.getId())
            .url(media.getUrl())
            .type(media.getType())
            .build())
          .toList()
      )
      .reacted(reacted)
      .owner(owner)
      .taggedUsers(taggedUsers)
      .build();
  }
}

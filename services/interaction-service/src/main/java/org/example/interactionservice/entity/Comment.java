package org.example.interactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.entity.Base.BaseEntityWithUpdate;
import org.hibernate.annotations.DynamicInsert;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "comments")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntityWithUpdate {
  @ManyToOne
  @JoinColumn(name = "parent_id")
  @EqualsAndHashCode.Exclude
  Comment parent;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  @EqualsAndHashCode.Exclude
  Post post;

  @Column(name = "user_id")
  Long userId;

  @Column(name = "content_text")
  String contentText;

  @Column(name = "child_count")
  Long childCount;

  Integer level;

  @Builder.Default
  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @ToString.Exclude
  Set<Comment> comments = new HashSet<>();
}

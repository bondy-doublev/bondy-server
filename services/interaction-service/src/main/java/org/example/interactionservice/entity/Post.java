package org.example.interactionservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference
    Set<MediaAttachment> mediaAttachments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    Set<Reaction> reactions = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    Set<Share> shares = new HashSet<>();
}

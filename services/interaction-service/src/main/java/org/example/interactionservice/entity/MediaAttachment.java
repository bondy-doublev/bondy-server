package org.example.interactionservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.entity.Base.BaseEntity;
import org.hibernate.annotations.DynamicInsert;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "media_attachments")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class MediaAttachment extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @JsonBackReference
    Post post;

    String type;
    String url;
}

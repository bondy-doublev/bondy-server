package org.example.interactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.entity.Base.BaseEntity;
import org.hibernate.annotations.DynamicInsert;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "mentions")
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Mention extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "post_id")
    @EqualsAndHashCode.Exclude
    Post post;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    @EqualsAndHashCode.Exclude
    Comment comment;

    @Column(name = "user_id")
    Long userId;
}

package org.example.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.entity.BaseEntity;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "api_keys")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey extends BaseEntity {
    String name;

    @Column(name = "key_hash")
    String keyHash;

    String prefix;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;

    Boolean active;
}

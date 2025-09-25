package org.example.authservice.entity;

import org.example.authservice.entity.Base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "otp_codes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"subject_id", "purpose"})
})
@DynamicInsert
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class OtpCode extends BaseEntity {
    @Column(name = "subject_type")
    String subjectType;

    @Column(name = "subject_id", nullable = false)
    Long subjectId;

    @Column(name = "purpose", nullable = false)
    String purpose;

    @Column(name = "code_hash")
    String codeHash;

    Integer attempts;
    Boolean active;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;
}

package org.example.communicationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageAttachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", nullable = false)
  Message message;

  @Column(nullable = false)
  String url;

  @Column(name = "file_name")
  String fileName;

  @Column(name = "mime_type")
  String mimeType;

  @Column(name = "file_size")
  Long size;

  // Nếu là ảnh, có thể có dimension (tuỳ ý)
  Integer width;
  Integer height;

  @Column(name = "created_at")
  LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = LocalDateTime.now();
  }
}
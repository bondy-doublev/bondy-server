package org.example.communicationservice.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
  Long id;
  Long conversationId;
  Long senderId;
  String type;         // TEXT/IMAGE/FILE
  String content;      // null nếu isDeleted=true
  List<AttachmentResponse> attachments;

  LocalDateTime createdAt;

  boolean edited;
  LocalDateTime editedAt;

  boolean deleted;
  LocalDateTime deletedAt;
}
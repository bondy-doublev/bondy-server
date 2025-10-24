package org.example.communicationservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LastMessageBriefResponse {
  Long id;
  Long senderId;
  String type;          // TEXT/IMAGE/FILE
  String content;       // can be null if deleted or non-text without caption
  LocalDateTime createdAt;
}
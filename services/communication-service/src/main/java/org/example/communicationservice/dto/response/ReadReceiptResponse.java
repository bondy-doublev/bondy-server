package org.example.communicationservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadReceiptResponse {
  Long conversationId;
  Long readerId;
  LocalDateTime readAt;
}
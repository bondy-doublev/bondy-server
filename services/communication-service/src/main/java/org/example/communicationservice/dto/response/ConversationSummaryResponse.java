package org.example.communicationservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationSummaryResponse {
  Long id;
  String type; // PRIVATE (hoặc khác trong tương lai)
  Long receiverId; // NEW: user còn lại trong cuộc trò chuyện PRIVATE (nếu không phải PRIVATE → null)
  LastMessageBriefResponse lastMessage; // có thể null nếu chưa có tin nhắn
}
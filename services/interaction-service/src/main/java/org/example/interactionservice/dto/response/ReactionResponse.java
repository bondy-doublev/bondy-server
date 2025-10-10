package org.example.interactionservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ReactionResponse {
  Long id;
  Long userId;
  Long postId;
  LocalDateTime createdAt;
}


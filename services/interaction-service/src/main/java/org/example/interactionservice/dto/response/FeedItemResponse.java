package org.example.interactionservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedItemResponse {
  String type;
  Double score;
  Long id;
  UserBasicResponse user;
  PostResponse post;
  LocalDateTime createdAt;
}


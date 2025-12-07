package org.example.interactionservice.dto.request;

import lombok.Data;

@Data
public class ShareCreateRequest {
  private String message;
  private Boolean isPublic;
}

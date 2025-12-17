package org.example.authservice.dto.response;

import lombok.Data;

@Data
public class UploadResponse {
  private boolean success;
  private int code;
  private String data; // <-- đường dẫn / url avatar
}

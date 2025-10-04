package org.example.commonweb.DTO.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.SuccessCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppApiResponse {
  boolean success = true;

  int code = SuccessCode.OK.getCode();

  Object data;

  public AppApiResponse(Object data) {
    this.code = SuccessCode.OK.getCode();
    this.data = data;
  }
}

package org.example.gateway.exception;

import org.example.commonweb.DTO.core.AppApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(WebClientResponseException.class)
  ResponseEntity<AppApiResponse> handlingWebClient(WebClientResponseException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAs(AppApiResponse.class));
  }
}

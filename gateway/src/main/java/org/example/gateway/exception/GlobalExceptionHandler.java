package org.example.gateway.exception;

import org.example.commonweb.DTO.core.ApiResponse;
import org.example.commonweb.DTO.core.ErrorResponse;
import org.example.commonweb.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WebClientResponseException.class)
    ResponseEntity<ApiResponse> handlingWebClient(WebClientResponseException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAs(ApiResponse.class));
    }
}

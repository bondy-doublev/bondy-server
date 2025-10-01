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
        ErrorCode errorCode;
        String message;

        if (ex.getStatusCode().is5xxServerError()) {
            errorCode = ErrorCode.INTERNAL_ERROR;
            message = "Server error from external service";
        } else {
            errorCode = ErrorCode.INTERNAL_ERROR;
            message = ex.getMessage();
        }

        ApiResponse response = ApiResponse.builder()
                .code(errorCode.getCode())
                .data(ErrorResponse.builder()
                        .type(errorCode.name())
                        .message(message)
                        .build())
                .build();

        return ResponseEntity.status(errorCode.getCode()).body(response);
    }
}

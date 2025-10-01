package org.example.authservice.exception;

import org.example.commonweb.DTO.core.ApiResponse;
import org.example.commonweb.DTO.core.ErrorResponse;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        ApiResponse response = ApiResponse.builder()
                .code(errorCode.getCode())
                .data(ErrorResponse.builder()
                        .type(errorCode.name())
                        .message(exception.getMessage())
                        .build())
                .build();
        return ResponseEntity.status(errorCode.getCode()).body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        ApiResponse response = ApiResponse.builder()
                .code(errorCode.getCode())
                .data(ErrorResponse.builder()
                        .type(errorCode.name())
                        .message(message)
                        .build())
                .build();

        return ResponseEntity.status(errorCode.getCode()).body(response);
    }

    @ExceptionHandler(WebClientResponseException.class)
    ResponseEntity<ApiResponse> handlingWebClient(WebClientResponseException ex) {
        ErrorCode errorCode;
        String message;

        if (ex.getStatusCode().value() == 401) {
            errorCode = ErrorCode.UNAUTHORIZED;
            message = "Unauthorized when calling external service";
        } else if (ex.getStatusCode().is4xxClientError()) {
            errorCode = ErrorCode.BAD_REQUEST;
            message = "Client error when calling external service";
        } else if (ex.getStatusCode().is5xxServerError()) {
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

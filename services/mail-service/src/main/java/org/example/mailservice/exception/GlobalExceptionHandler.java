package org.example.mailservice.exception;

import org.example.commonweb.DTO.core.ApiResponse;
import org.example.commonweb.DTO.core.ErrorResponse;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

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

    @ExceptionHandler(SQLException.class)
    ResponseEntity<ApiResponse> handlingSQL(SQLException ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        String message = ex.getMessage();

        ApiResponse response = ApiResponse.builder()
                .code(errorCode.getCode())
                .data(ErrorResponse.builder()
                        .type(errorCode.name())
                        .message(message)
                        .build())
                .build();

        return ResponseEntity.status(errorCode.getCode()).body(response);
    }

    @ExceptionHandler(HttpMediaTypeException.class)
    ResponseEntity<ApiResponse> handlingHttpMediaTypeException(HttpMediaTypeException ex) {
        ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;

        String message = ex.getMessage() != null ? ex.getMessage() : "Unsupported media type";

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

package org.example.notificationservice.exception;

import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.commonweb.DTO.core.AppErrorResponse;
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
  ResponseEntity<AppApiResponse> handlingRuntimeException(AppException exception) {
    ErrorCode errorCode = exception.getErrorCode();

    AppApiResponse response = AppApiResponse.builder()
      .code(errorCode.getCode())
      .data(AppErrorResponse.builder()
        .type(errorCode.name())
        .message(exception.getMessage())
        .build())
      .build();
    return ResponseEntity.status(errorCode.getCode()).body(response);
  }

  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  ResponseEntity<AppApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult()
      .getFieldErrors()
      .stream()
      .findFirst()
      .map(DefaultMessageSourceResolvable::getDefaultMessage)
      .orElse("Validation error");

    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

    AppApiResponse response = AppApiResponse.builder()
      .code(errorCode.getCode())
      .data(AppErrorResponse.builder()
        .type(errorCode.name())
        .message(message)
        .build())
      .build();

    return ResponseEntity.status(errorCode.getCode()).body(response);
  }

  @ExceptionHandler(SQLException.class)
  ResponseEntity<AppApiResponse> handlingSQL(SQLException ex) {
    ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
    String message = ex.getMessage();

    AppApiResponse response = AppApiResponse.builder()
      .code(errorCode.getCode())
      .data(AppErrorResponse.builder()
        .type(errorCode.name())
        .message(message)
        .build())
      .build();

    return ResponseEntity.status(errorCode.getCode()).body(response);
  }

  @ExceptionHandler(HttpMediaTypeException.class)
  ResponseEntity<AppApiResponse> handlingHttpMediaTypeException(HttpMediaTypeException ex) {
    ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;

    String message = ex.getMessage() != null ? ex.getMessage() : "Unsupported media type";

    AppApiResponse response = AppApiResponse.builder()
      .code(errorCode.getCode())
      .data(AppErrorResponse.builder()
        .type(errorCode.name())
        .message(message)
        .build())
      .build();

    return ResponseEntity.status(errorCode.getCode()).body(response);
  }

}

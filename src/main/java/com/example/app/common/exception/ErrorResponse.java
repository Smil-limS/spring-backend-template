package com.example.app.common.exception;

import java.time.Instant;
import java.util.List;

/** 모든 에러 응답의 공통 형식. */
public record ErrorResponse(
    Instant timestamp, int status, String code, String message, List<FieldError> errors) {

  public record FieldError(String field, String reason) {}

  public static ErrorResponse of(ErrorCode errorCode) {
    return of(errorCode, errorCode.getMessage(), List.of());
  }

  public static ErrorResponse of(ErrorCode errorCode, String message) {
    return of(errorCode, message, List.of());
  }

  public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldError> errors) {
    return new ErrorResponse(
        Instant.now(), errorCode.getStatus().value(), errorCode.getCode(), message, errors);
  }
}

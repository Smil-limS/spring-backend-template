package com.example.app.common.exception;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
    ErrorCode code = e.getErrorCode();
    log.warn("[{}] {}", code.getCode(), e.getMessage());
    return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    List<ErrorResponse.FieldError> errors =
        e.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
            .toList();
    ErrorCode code = ErrorCode.INVALID_INPUT;
    return ResponseEntity.status(code.getStatus())
        .body(ErrorResponse.of(code, code.getMessage(), errors));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    log.error("처리되지 않은 예외", e);
    ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
  }
}

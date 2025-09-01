package com.tsb.banking.exception.advice;

import com.tsb.banking.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handle(NotFoundException e) {
    return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
  }
}

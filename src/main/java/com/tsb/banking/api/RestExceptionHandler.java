package com.tsb.banking.api;

import com.tsb.banking.exception.NotFound;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {
  @ExceptionHandler(NotFound.class)
  public ResponseEntity<Map<String, String>> handle(NotFound e) {
    return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
  }
}

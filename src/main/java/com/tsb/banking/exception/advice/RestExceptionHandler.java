package com.tsb.banking.exception.advice;

import com.tsb.banking.exception.BusinessException;
import com.tsb.banking.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusiness(BusinessException e) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    pd.setTitle("Business Error");
    pd.setDetail(e.getMessage());
    pd.setProperty("code", e.getCode());
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleOthers(Exception e) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Internal Error");
    pd.setDetail("Unexpected error");
    return pd;
  }
}

package com.example.elasticsearchservice.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("Beklenmeyen hata oluştu", ex);
        return ResponseEntity
                .internalServerError()
                .body(new ErrorResponse("Bir hata oluştu: " + ex.getMessage()));
    }

    record ErrorResponse(String message) {}
} 
package com.example.backend.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 예약 불가능할 때
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", e.getMessage()); // 이미 짧은 메시지만 들어옴
        return ResponseEntity.badRequest().body(body);
    }
    
    // 문서가 존재하지 않을 때
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    // 500 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 500);
        body.put("error", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.internalServerError().body(body);
    }
}


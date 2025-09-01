package com.example.backend.common.exception;

public class BusinessException extends RuntimeException {
    private final ErrorCode code;
    public BusinessException(ErrorCode code, String msg){ super(msg); this.code = code; }
    public ErrorCode getCode(){ return code; }
}
package com.tjoeun.boxmon.exception;

public class ConcurrentRequestExceedException extends RuntimeException {
    public ConcurrentRequestExceedException(String message) {
        super(message);
    }
    public ConcurrentRequestExceedException(String message, Throwable cause) {
        super(message, cause);
    }
}

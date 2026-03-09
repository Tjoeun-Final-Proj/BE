package com.tjoeun.boxmon.exception;

import lombok.Getter;

public class RateLimitExceededException extends RuntimeException {
    @Getter
    private String RetryAfter;
    
    public RateLimitExceededException(String message) {
        super(message);
    }
    public RateLimitExceededException(String message, String RetryAfter) {
        super(message);
        this.RetryAfter = RetryAfter;
    }
}

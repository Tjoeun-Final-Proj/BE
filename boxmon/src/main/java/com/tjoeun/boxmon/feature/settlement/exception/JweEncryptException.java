package com.tjoeun.boxmon.feature.settlement.exception;

import com.tjoeun.boxmon.exception.ExternalServiceException;

public class JweEncryptException extends ExternalServiceException {
    public JweEncryptException(String message) {
        super(message);
    }
    public JweEncryptException(String message, Throwable cause) {
        super(message, cause);
    }
}

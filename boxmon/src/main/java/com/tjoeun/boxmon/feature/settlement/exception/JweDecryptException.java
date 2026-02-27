package com.tjoeun.boxmon.feature.settlement.exception;

import com.tjoeun.boxmon.exception.ExternalServiceException;

public class JweDecryptException extends ExternalServiceException {
    public JweDecryptException(String message) {
        super(message);
    }
    public JweDecryptException(String message, Throwable cause) { 
        super(message, cause);
    }
}

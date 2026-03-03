package com.tjoeun.boxmon.exception;

public class ContactNotFoundException extends RuntimeException{
    public ContactNotFoundException(String message) {
        super(message);
    }
}

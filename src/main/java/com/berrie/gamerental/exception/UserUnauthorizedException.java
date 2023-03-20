package com.berrie.gamerental.exception;

public class UserUnauthorizedException extends RuntimeException {

    public UserUnauthorizedException(String message) {
        super(message);
    }

    public UserUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

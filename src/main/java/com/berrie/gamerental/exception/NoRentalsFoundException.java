package com.berrie.gamerental.exception;

public class NoRentalsFoundException extends RuntimeException {

    public NoRentalsFoundException(String message) {
        super(message);
    }
}

package com.berrie.gamerental.controller;

import com.berrie.gamerental.exception.UserExistsException;
import com.berrie.gamerental.exception.UserUnauthorizedException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler controller for handling exceptions thrown by any controller in the application.
 */
@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errorMessages = bindingResult.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, String.valueOf(errorMessages)).build();
    }

    @ExceptionHandler(UserExistsException.class)
    public ErrorResponse handleUserExistsException(UserExistsException ex) {
        return ErrorResponse.builder(ex, HttpStatus.CONFLICT, ex.getMessage()).build();
    }

    @ExceptionHandler(UserUnauthorizedException.class)
    public ErrorResponse handleUserUnauthorizedException(UserUnauthorizedException ex) {
        return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage()).build();
    }
}

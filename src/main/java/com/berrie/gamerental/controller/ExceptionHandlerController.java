package com.berrie.gamerental.controller;

import com.berrie.gamerental.exception.NoGamesFoundException;
import com.berrie.gamerental.exception.UserExistsException;
import com.berrie.gamerental.exception.UserUnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errorMessages = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
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

    @ExceptionHandler(NoGamesFoundException.class)
    public ErrorResponse handleNoGamesFoundException(NoGamesFoundException ex) {
        return ErrorResponse.builder(ex, HttpStatus.NOT_FOUND, ex.getMessage()).build();
    }
}

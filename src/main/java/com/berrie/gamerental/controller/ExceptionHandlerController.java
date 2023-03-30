package com.berrie.gamerental.controller;

import com.berrie.gamerental.exception.*;
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

    @ExceptionHandler(GameSubmissionException.class)
    public ErrorResponse handleGameSubmissionException(GameSubmissionException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage()).build();
    }

    @ExceptionHandler({UserExistsException.class, GameRentedException.class, GameReturnedException.class})
    public ErrorResponse handleConflictExceptions(Exception ex) {
        return ErrorResponse.builder(ex, HttpStatus.CONFLICT, ex.getMessage()).build();
    }

    @ExceptionHandler({NoGamesFoundException.class, NoRentalsFoundException.class})
    public ErrorResponse handleNotFoundExceptions(Exception ex) {
        return ErrorResponse.builder(ex, HttpStatus.NOT_FOUND, ex.getMessage()).build();
    }

    @ExceptionHandler(UserUnauthorizedException.class)
    public ErrorResponse handleUserUnauthorizedException(UserUnauthorizedException ex) {
        return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage()).build();
    }
}

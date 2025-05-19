package ru.practicum.explorewithme.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ErrorsHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Incorrectly made request.")
                .timestamp(LocalDateTime.now())
                .status(BAD_REQUEST)
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ApiError handleDataValidationException(ValidationException ex) {
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Incorrectly made request.")
                .timestamp(LocalDateTime.now())
                .status(BAD_REQUEST)
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("The required object was not found.")
                .timestamp(LocalDateTime.now())
                .status(NOT_FOUND)
                .build();
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(NOT_FOUND)
    public ApiError handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("The required object was not found.")
                .timestamp(LocalDateTime.now())
                .status(NOT_FOUND)
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(CONFLICT)
    public ApiError handleConflictException(ConflictException ex) {
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("For the requested operation the conditions are not met.")
                .timestamp(LocalDateTime.now())
                .status(CONFLICT)
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(CONFLICT)
    public ApiError handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("Integrity constraint has been violated.")
                .timestamp(LocalDateTime.now())
                .status(CONFLICT)
                .build();
    }

    @ExceptionHandler(PublicationException.class)
    @ResponseStatus(FORBIDDEN)
    public ApiError handlePublishException(PublicationException ex) {
        return ApiError.builder()
                .message(ex.getMessage())
                .reason("For the requested operation the conditions are not met.")
                .timestamp(LocalDateTime.now())
                .status(FORBIDDEN)
                .build();
    }
}
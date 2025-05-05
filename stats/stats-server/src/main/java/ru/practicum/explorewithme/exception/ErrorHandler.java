package ru.practicum.explorewithme.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;


@RestControllerAdvice
public class ErrorHandler {
    private ApiError handleException(final Exception e, HttpStatus status) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();
        return new ApiError(status, "Error...", e.getMessage(), stackTrace);
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        //log.info("500 {}", e.getMessage(), e);
        return handleException(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleIncorrectParameter(final NotFoundException e) {
        //log.info("500 {}", e.getMessage(), e);
        return handleException(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final BadRequestException e) {
        //log.info("500 {}", e.getMessage(), e);
        return handleException(e, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServerException(
            final InternalServerException e) {
        //log.info("500 {}", e.getMessage(), e);
        return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    class ApiError {
        HttpStatus status;
        String error;
        String message;
        String stackTrace;

        public ApiError(HttpStatus status, String error,
                        String message, String stackTrace) {
            this.error = error;
            this.status = status;
            this.message = message;
            this.stackTrace = stackTrace;
        }

        @Override
        public String toString() {
            return status + " " + error + " "
                    + message + " " + stackTrace;
        }
    }
}

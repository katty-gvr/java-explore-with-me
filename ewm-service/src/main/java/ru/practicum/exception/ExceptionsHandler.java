package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.info("Not found error: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации данных: " + e.getMessage(), HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final TimeValidationException e) {
        log.info("Conflict error: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации данных: " + e.getMessage(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final ConflictException e) {
        log.info("Conflict error: {}", e.getMessage());
        return new ErrorResponse("Конфликт данных: " + e.getMessage(), HttpStatus.CONFLICT.getReasonPhrase());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConstraintViolationException(final SQLIntegrityConstraintViolationException e) {
        log.info("Conflict error: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), HttpStatus.CONFLICT.getReasonPhrase());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.error("Error parsing request body: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), HttpStatus.CONFLICT.getReasonPhrase());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(final BadRequestException e) {
        log.info("Bad request error: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации данных: " + e.getMessage(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse hadleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.info("Data integrity violation error: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), HttpStatus.CONFLICT.getReasonPhrase());
    }
}

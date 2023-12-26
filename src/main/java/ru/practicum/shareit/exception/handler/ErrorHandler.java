package ru.practicum.shareit.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.exception.EmailIsAlreadyRegisteredException;
import ru.practicum.shareit.exception.SearchQueryException;


@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDataNotFoundException(final DataNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleSearchQueryException(final SearchQueryException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataValidationException(final DataValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailIsAlreadyRegisteredException
            (final EmailIsAlreadyRegisteredException e) {
        return new ErrorResponse(e.getMessage());
    }
}

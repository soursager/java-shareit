package ru.practicum.shareit.exception;

public class DataValidationException extends RuntimeException {
    public DataValidationException(String message) {
        super(message);
    }
}

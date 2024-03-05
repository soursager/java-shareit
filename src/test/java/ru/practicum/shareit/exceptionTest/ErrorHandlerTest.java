package ru.practicum.shareit.exceptionTest;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.exception.handler.ErrorHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorHandlerTest {
    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    public void testHandleUserNotFoundException() {
        DataNotFoundException exception = new DataNotFoundException("Пользователь не существует!");
        String result = errorHandler.handleDataNotFoundException(exception).getError();
        assertEquals("Пользователь не существует!", result);
    }

    @Test
    public void testHandleSearchQueryException() {
        SearchQueryException exception = new SearchQueryException("Ничего не найдено!");
        String result = errorHandler.handleSearchQueryException(exception).getError();
        assertEquals("Ничего не найдено!", result);
    }

    @Test
    public void testHandleDataValidationException() {
        DataValidationException exception = new DataValidationException("Элемент неверный");
        String result = errorHandler.handleDataValidationException(exception).getError();
        assertEquals("Элемент неверный", result);
    }

    @Test
    public void testРandlePageValidationException() {
        IncorrectNumberPageException exception = new IncorrectNumberPageException("Неверное число страницы");
        String result = errorHandler.handlePageValidationException(exception).getError();
        assertEquals("Неверное число страницы", result);
    }

    @Test
    public void testHandleUnsupportedStateException() {
        UnsupportedStatusException exception = new UnsupportedStatusException("Неверный статус");
        String result = errorHandler.handleDataStatusException(exception).getError();
        assertEquals("Неверный статус", result);
    }

    @Test
    public void testHandleEmailIsAlreadyRegisteredException() {
        EmailIsAlreadyRegisteredException exception = new EmailIsAlreadyRegisteredException("Емейл уже зарегистрирован");
        String result = errorHandler.handleEmailIsAlreadyRegisteredException(exception).getError();
        assertEquals("Емейл уже зарегистрирован", result);
    }
}

package ru.practicum.shareit.validator;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.IncorrectNumberPageException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PageValidatorTest {

    @Test
    void checkingPageableParams_whenParamsIncorrect_thenThrowIncorrectDataException() {
        PageValidatorService validator = new PageValidatorService();

        IncorrectNumberPageException exception = assertThrows(IncorrectNumberPageException.class,
                () -> validator.checkingPageableParams(-1, -2));

        assertEquals(exception.getMessage(), "Размер страницы не может быть меньше нуля");
    }
}

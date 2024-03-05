package ru.practicum.shareit.validator;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.IncorrectNumberPageException;

@Component
public class PageValidatorService {
    public void checkingPageableParams(Integer from, Integer size) {
        if (size < 0 || from < 0) {
            throw new IncorrectNumberPageException("Размер страницы не может быть меньше нуля");
        }
    }
}

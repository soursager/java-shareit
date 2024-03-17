package ru.practicum.shareit.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.request.ItemRequestDto;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    public void validateItemRequestData(ItemRequestDto requestDto) {
        if (requestDto.getDescription() == null || requestDto.getDescription().isEmpty()) {
            throw new DataValidationException("Описание запроса не может быть пустым!");
        }
    }
}
package ru.practicum.shareit.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;

@Component
@RequiredArgsConstructor
public class RequestValidatorService {

    private final RequestRepository repository;

    public void validateItemRequestId(long requestId) {
        findItemRequestById(requestId);
    }

    public ItemRequest validateItemRequestIdAndReturn(long requestId) {
        return findItemRequestById(requestId);
    }

    public void validateItemRequestData(ItemRequestDto requestDto) {
        if (requestDto.getDescription() == null || requestDto.getDescription().isEmpty()) {
            throw new DataValidationException("Описание запроса вещи не может быть пустым!");
        }
    }

    private ItemRequest findItemRequestById(long requestId) {
        if (requestId < 0) {
            throw new DataValidationException("Не может существовать запроса с номером меньше 0");
        }
        return repository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Не запроса с номером: " + requestId));
    }
}
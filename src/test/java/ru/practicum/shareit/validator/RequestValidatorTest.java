package ru.practicum.shareit.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestValidatorTest {
    @Mock
    RequestRepository requestRepository;

    @InjectMocks
    RequestValidator requestValidator;

    @Test
    void validateItemRequestId_whenRequestNotExists_thenThrowEntityNotFoundException() {
        when(requestRepository.findById(anyLong())).thenThrow(new DataNotFoundException("Запрос не существует"));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> requestValidator.validateItemRequestId(1L));

        assertEquals(exception.getMessage(), "Запрос не существует");
    }


    @Test
    void validateItemRequestId_whenRequestIdLessThanZero_thenThrowIncorrectDataException() {
        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> requestValidator.validateItemRequestId(-1L));

        assertEquals(exception.getMessage(), "Не может существовать запроса с номером меньше 0");
    }

    @Test
    void validateItemRequestIdAndReturns_whenRequestExists_thenReturnRequest() {
        ItemRequest request = new ItemRequest();
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));

        ItemRequest actual =  requestValidator.validateItemRequestIdAndReturns(1L);

        assertEquals(actual, request);
    }

    @Test
    void validateItemRequestIdAndReturns_whenRequestNotExists_thenThrowEntityNotFoundException() {
        when(requestRepository.findById(anyLong())).thenThrow(new DataNotFoundException("Не запроса с таким номером"));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> requestValidator.validateItemRequestIdAndReturns(1L));

        assertEquals(exception.getMessage(), "Не запроса с таким номером");
    }

    @Test
    void validateItemRequestIdAndReturns_whenRequestIdLessThanZero_thenThrowIncorrectDataException() {
        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> requestValidator.validateItemRequestIdAndReturns(-1L));

        assertEquals(exception.getMessage(), "Не может существовать запроса с номером меньше 0");
    }

    @Test
    void validateItemRequestData_whenDataIncorrect_throwIncorrectDataException() {
        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> requestValidator.validateItemRequestData(new ItemRequestDto()));

        assertEquals(exception.getMessage(), "Описание запроса вещи не может быть пустым!");
    }
}

package ru.practicum.shareit.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.repository.ItemRepository;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemValidatorTest {
    @Mock
    ItemRepository repository;

    @InjectMocks
    ItemValidator itemValidator;


    @Test
    void validateItemId_whenItemNotExists_thenThrowEntityNotFoundException() {
        when(repository.findById(anyLong())).thenThrow(new DataNotFoundException("Такого предмета не существует!"));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> itemValidator.validateItemId(1L));

        assertEquals(exception.getMessage(), "Такого предмета не существует!");
    }

    @Test
    void validateItemId_whenItemIdLessThanZero_thenThrowIncorrectDataException() {
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> itemValidator.validateItemId(-1L));

        assertEquals(exception.getMessage(), "Не может существовать вещи с номером меньше 0");
    }

    @Test
    void validateCommentData_whenDataIsIncorrect_thanThrowIncorrectDataException() {
        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> itemValidator.validateCommentData(new CommentDto()));

        assertEquals(exception.getMessage(), "Комментарий не может быть пустым");
    }
}

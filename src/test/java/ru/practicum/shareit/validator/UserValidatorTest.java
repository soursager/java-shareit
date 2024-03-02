package ru.practicum.shareit.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserValidatorTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserValidator userValidator;

    @Test
    void validateUserId_whenUserNotExists_thenThrowEntityNotFoundException() {
        when(userRepository.findById(anyLong())).thenThrow(new DataNotFoundException("Пользователя не существует!"));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userValidator.checkingUserId(1L));

        assertEquals(exception.getMessage(), "Пользователя не существует!");
    }


    @Test
    void validateUserId_whenUserIdLessThanZero_thenThrowIncorrectDataException() {
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userValidator.checkingUserId(-1L));

        assertEquals(exception.getMessage(), "Пользователя под номером : -1 не существует!");
    }

    @Test
    void validateUserIdAndReturns_whenUserNotExists_thenThrowEntityNotFoundException() {
        when(userRepository.findById(anyLong())).thenThrow(new DataNotFoundException("Пользователя под номером : " +
                "1 не существует!"));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userValidator.returnUserIfExists(1L));

        assertEquals(exception.getMessage(), "Пользователя под номером : 1 не существует!");
    }

    @Test
    void validateUserIdAndReturns_whenUserIdLessThanZero_thenThrowIncorrectDataException() {
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userValidator.returnUserIfExists(-1L));

        assertEquals(exception.getMessage(), "Пользователя под номером : -1 не существует!");
    }

    @Test
    void validateUserData_returnUserIfExists() {
       DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userValidator.checkingUserIdAndNotReturns(1L));

        assertEquals(exception.getMessage(), "Пользователя под номером : 1 не существует!");
    }
}

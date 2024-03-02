package ru.practicum.shareit.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.validator.UserValidator;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.user.dto.UserMapper.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private User user;
    private User updateUser;

    @Mock
    UserRepository userRepository;

    @Mock
    UserValidator userValidator;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    public void fillData() {
        user = User.builder()
                .id(1L)
                .name("name")
                .email("mail@Mail.ru")
                .build();

        updateUser = User.builder()
                .id(1L)
                .name("name2")
                .email("mail@Mail2.ru")
                .build();
    }


    @Test
    void create_whenAllDataIsCorrect_thenReturnCorrectUser() {
        User expectedUser = new User();
        expectedUser.setEmail("test@mail.ru");
        when(userRepository.save(expectedUser)).thenReturn(expectedUser);

        UserDto actualUser = userService.createUserDto(toUserDto(expectedUser));

        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
    }

    @Test
    void create_whenDataIsIncorrect_thenThrowEmptyFieldExceptionException() {
        User expectedUser = new User();
        when(userRepository.save(expectedUser)).thenThrow(new DataNotFoundException("Адрес почты пуст!"));

        DataNotFoundException emptyFieldException = assertThrows(DataNotFoundException.class,
                () -> userService.createUserDto(toUserDto(expectedUser)));
        assertEquals(emptyFieldException.getMessage(), "Адрес почты пуст!");
    }

    @Test
    void getById_whenUserFound_thenReturnUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto actualUser = userService.getUserDto(1L);

        assertEquals(toUserDto(user), actualUser);
    }

    @Test
    void getById_whenUserNotFound_thenThrowEntityNotFoundException() {
        when(userRepository.findById(anyLong()))
                .thenThrow(new DataNotFoundException("Пользователя не существует!"));

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> userService.getUserDto(1L));

        assertEquals(dataNotFoundException.getMessage(), "Пользователя не существует!");
    }

    @Test
    void getAll_whenDataExists_thenReturnEmptyCollection() {
        when(userService.getUsersDto()).thenReturn(Collections.emptyList());

        Collection<UserDto> users = userService.getUsersDto();

        assertEquals(users.size(), 0);
    }

    @Test
    void update_whenUserExists_thenReturnUpdatedUser() {
        when(userValidator.returnUserIfExists(1L)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(updateUser);

        UserDto actualUser = userService.updateUserDto(toUserDto(updateUser),1L);

        assertEquals(toUserDto(updateUser), actualUser);
        verify(userRepository, times(1))
                .save(updateUser);
    }

    @Test
    void update_whenUserNotExists_thenThrowEntityNotFoundException() {
        when(userValidator.returnUserIfExists(1L))
                .thenThrow(new DataNotFoundException("Пользователь не существует!"));

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> userService.updateUserDto(toUserDto(updateUser), 1L));

        assertEquals(dataNotFoundException.getMessage(), "Пользователь не существует!");
    }

    @Test
    void delete_whenUserExists_thenDeleteUser() {
        userService.delete(1L);
        verify(userRepository, times(1))
                .deleteById(1L);
    }

    @Test
    void delete_whenUserNotExists_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Данного пользователя нет в базе!"))
                .when(userValidator).checkingUserIdAndNotReturns(anyLong());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> userService.delete(1L));

        assertEquals(dataNotFoundException.getMessage(), "Данного пользователя нет в базе!");
    }
}

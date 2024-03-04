package ru.practicum.shareit.usersTest.dtoTest;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {

    @Test
    void toUserDto() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("mail@Mail.ru")
                .build();
        User userFromConstructor = new User(1L, "name", "mail@mail.ru");

        UserDto userDto = UserMapper.toUserDto(user);
        UserDto userDtoConstructor = UserMapper.toUserDto(userFromConstructor);

        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(userDtoConstructor.getName(), userFromConstructor.getName());
    }

    @Test
    void toUser() {
        UserDto userDto = UserDto.builder()
                .id(2L)
                .email("email@mail.ru")
                .name("name")
                .build();
        User user = UserMapper.toUser(userDto);

        assertEquals(userDto.getId(), user.getId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }
}

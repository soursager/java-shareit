package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto createUserDto(UserDto userDto);

    UserDto getUserDto(long id);

    Collection<UserDto> getUsersDto();

    UserDto updateUserDto(UserDto user, Long userId);

    void delete(long id);
}

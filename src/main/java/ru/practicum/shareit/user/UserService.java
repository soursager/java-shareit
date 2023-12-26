package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    public List<UserDto> getUsersDto() {
        return userStorage.getAllUsers().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserDto(Long id) {
        return userMapper.toUserDto(userStorage.getUserById(id));
    }

    public  UserDto createUserDto(UserDto userDto) {
        return userMapper.toUserDto(userStorage.create(userMapper.toUser(userDto)));
    }

    public UserDto updateUserDto(UserDto userDto, Long userId) {
        return userMapper.toUserDto(userStorage.update(userMapper.toUser(userDto)));
    }

    public void delete(Long id) {
        userStorage.delete(id);
    }

}

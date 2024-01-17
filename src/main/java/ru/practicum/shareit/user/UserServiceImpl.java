package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public UserDto createUserDto(UserDto userDto) {
        return userMapper.toUserDto(userStorage.create(userMapper.toUser(userDto)));
    }

    @Override
    public UserDto getUserDto(long id) {
        return userMapper.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public Collection<UserDto> getUsersDto() {
        return userStorage.getAllUsers().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUserDto(UserDto userDto, Long userId) {
        return userMapper.toUserDto(userStorage.update(userMapper.toUser(userDto)));
    }

    @Override
    public void delete(long id) {
        userStorage.delete(id);
    }
}

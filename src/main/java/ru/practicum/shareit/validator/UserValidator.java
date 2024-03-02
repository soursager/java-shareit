package ru.practicum.shareit.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;

    public UserDto checkingUserId(Long userId) {
        if (userId == -1) {
            throw new DataNotFoundException("Пользователя под номером : " + userId + " не существует!");
        }
        return UserMapper.toUserDto(userRepository.findById(userId).orElseThrow(() ->
                new DataNotFoundException("Пользователя под номером : " + userId + " не существует!")));
    }

    public void checkingUserIdAndNotReturns(Long userId) {
        if (userId == -1) {
            throw new DataNotFoundException("Пользователя под номером : " + userId + " не существует!");
        }
         UserMapper.toUserDto(userRepository.findById(userId).orElseThrow(() ->
                new DataNotFoundException("Пользователя под номером : " + userId + " не существует!")));
    }

    public User returnUserIfExists(Long userId) {
        if (userId == -1) {
            throw new DataNotFoundException("Пользователя под номером : " + userId + " не существует!");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь по id - " + userId + " не найден"));
    }
}

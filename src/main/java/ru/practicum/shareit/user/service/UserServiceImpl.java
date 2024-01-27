package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.exception.EmailIsAlreadyRegisteredException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto createUserDto(UserDto userDto) {
        try {
            return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            throw new EmailIsAlreadyRegisteredException("Пользователь с таким email уже зарегистрирован!");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserDto(long id) {
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Данного пользователя нет в базе!")));
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<UserDto> getUsersDto() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto updateUserDto(UserDto userDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Данного пользователя нет в базе!"));
        if (userDto.getId() == null) {
            throw new DataValidationException("Не передан номер пользователя!");
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if ((userDto.getEmail() != null) && (!userDto.getEmail().equals(user.getEmail()))) {
            if (checkingTheUserEmail(userDto)) {
                user.setEmail(userDto.getEmail());
            } else {
                throw new EmailIsAlreadyRegisteredException("Пользователь с E-mail="
                        + user.getEmail() + " уже существует!");
            }
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public void delete(long id) {
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("Данного пользователя нет в базе!");
        }
    }

    private boolean checkingTheUserEmail(UserDto userDto) {
        List<User> userForEmail = userRepository.findByEmail(userDto.getEmail());
        return userForEmail
                .stream()
                .filter(u -> u.getEmail().equals(userDto.getEmail()))
                .allMatch(u -> u.getId().equals(userDto.getId()));
    }
}

package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.EmailIsAlreadyRegisteredException;
import ru.practicum.shareit.exception.DataValidationException;

import java.util.*;

@Component
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {
    public  Map<Long, User> users = new HashMap<>();
    private Long userId = 0L;

    @Override
    public User create(User user) {
        if ((users.values().stream().noneMatch(u -> u.getEmail().equals(user.getEmail())))) {
            user.setId(nextId());
            users.put(userId, user);
        } else {
            throw new EmailIsAlreadyRegisteredException("Пользователь с таким email уже зарегистрирован!");
        }
        return user;
    }

    @Override
    public User update(User user) {
            if (user.getId() == null) {
                throw new DataValidationException("Не передан номер пользователя!");
            }
            if (!users.containsKey(user.getId())) {
                throw new DataNotFoundException("Данного пользователя нет в базе!");
            }
            if (user.getName() == null) {
                user.setName(users.get(user.getId()).getName());
            }
            if (user.getEmail() == null) {
                user.setEmail(users.get(user.getId()).getEmail());
            }
        if (users.values().stream()
                .filter(us -> us.getEmail().equals(user.getEmail()))
                .allMatch(us -> us.getId().equals(user.getId()))) {
            users.put(user.getId(), user);
        } else {
            throw new EmailIsAlreadyRegisteredException("Пользователь с E-mail="
                    + user.getEmail() + " уже существует!");
        }
        return user;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new DataNotFoundException("Не передан номер пользователя!");
        }
        if (!users.containsKey(id)) {
            throw new DataNotFoundException("Данного пользователя нет в базе!");
        }
        users.remove(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new DataNotFoundException("Данного пользователя нет в базе!");
        }
        return users.get(id);
    }

    public Long nextId() {
        userId++;
        return userId;
    }
}

package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {
    User create(User user);
    User update(User user);
    void delete(Long id);
    List<User> getAllUsers();
    User getUserById (Long id);

}

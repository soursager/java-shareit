package ru.practicum.shareit.usersTest.repositoryTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailTest() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email@mai.ru")
                .build();
        userRepository.save(user);
        List<User> findUserByEmail = userRepository.findByEmail("email@mai.ru");
        User userForEmail = findUserByEmail.get(0);
        assertEquals(user.getName(), userForEmail.getName());
        assertEquals(user.getEmail(), userForEmail.getEmail());
    }

    @Test
    void unsuccessfulSearchWithAnError() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email@mai.ru")
                .build();
        userRepository.save(user);
        List<User> findUserByEmail = userRepository.findByEmail("email2@mai.ru");
        assertEquals(findUserByEmail.size(), 0);
    }
}

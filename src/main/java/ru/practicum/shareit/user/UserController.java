package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Получение всех пользователей");
        return userService.getUsersDto();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Получение пользователя под номером: {}", userId);
        return userService.getUserDto(userId);
    }

    @ResponseBody
    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Добавление пользователя");
        return userService.createUserDto(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable long userId, @RequestBody UserDto userDto) {
        log.debug("Обновление данных пользователя под номером: {}", userId);
        userDto.setId(userId);
        return userService.updateUserDto(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        log.debug("Удаление пользователя под номером: {}", userId);
        userService.delete(userId);
    }
}

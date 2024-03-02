package ru.practicum.shareit.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @SneakyThrows
    @Test
    void getAllUsers() {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUsersDto();
    }

    @SneakyThrows
    @Test
    void getUserById() {
        long userId = 0L;
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserDto(userId);
    }

    @SneakyThrows
    @Test
    void createUser() {
        UserDto userToCreate = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@mai.ru")
                .build();
        when(userService.createUserDto(userToCreate)).thenReturn(userToCreate);

        String result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userToCreate), result);
    }

    @SneakyThrows
    @Test
    void updateUser() {
        long userId = 1L;
        UserDto userToCreate = new UserDto();
        userToCreate.setId(userId);
        UserDto userUpdated = UserDto.builder()
                .email("updated@mail.ru")
                .id(userId)
                .build();
        when(userService.updateUserDto(userToCreate, 1L))
                .thenReturn(userUpdated);

        String result = mockMvc.perform(patch("/users/{userId}", userId, userUpdated)
                        .content(objectMapper.writeValueAsString(userToCreate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userUpdated), result);
    }

    @SneakyThrows
    @Test
    void deleteUser() {
        long userId = 0L;
        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(userId);

   }
}

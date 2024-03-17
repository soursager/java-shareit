package ru.practicum.shareit.usersTest.controllersTest;

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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
   private ObjectMapper objectMapper;

    @Autowired
   private MockMvc mockMvc;

    @MockBean
   private UserService userService;

    @SneakyThrows
    @Test
    void getAllUsers() {
        UserDto user1 = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@mai.ru")
                .build();
        UserDto user2 = UserDto.builder()
                .id(2L)
                .name("name2")
                .email("email2@mai.ru")
                .build();
        List<UserDto> usersDto = new ArrayList<>();
        usersDto.add(user1);
        usersDto.add(user2);
        when(userService.getUsersDto())
                .thenReturn(usersDto);
        mockMvc.perform(get("/users")
                 .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().json(objectMapper.writeValueAsString(usersDto)));

        verify(userService, times(1)).getUsersDto();
    }

    @SneakyThrows
    @Test
    void getUserById() {
        UserDto userToCreate = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@mai.ru")
                .build();
        Long userId = 1L;
        when(userService.getUserDto(userId))
               .thenReturn(userToCreate);
        String result = mockMvc.perform(get("/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, times(1)).getUserDto(userId);
        assertEquals(objectMapper.writeValueAsString(userToCreate), result);
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

package ru.practicum.shareit.itemTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validator.PageValidator;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ItemService itemService;

    @MockBean
    PageValidator pageValidator;

    @Autowired
    private MockMvc mvc;

    @SneakyThrows
    @Test
    void createItem() {
        ItemDto itemDto = ItemDto.builder().id(1L)
                .description("text")
                .available(true)
                .name("name")
                .build();
        when(itemService.createItemDto(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        String result = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDto), result);
    }

    @SneakyThrows
    @Test
    void updateItem() {
        ItemDto itemToCreate = ItemDto.builder()
                .id(1L)
                .build();
        ItemDto itemToUpdate = ItemDto.builder()
                .id(2L)
                .build();
        when(itemService.updateItemDto(any(ItemDto.class), any(Long.class), any(Long.class)))
                .thenReturn(itemToUpdate);

        String result = mockMvc.perform(patch("/items/{itemId}", itemToCreate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToCreate))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemToUpdate), result);
    }

    @SneakyThrows
    @Test
    void getItemById() {
        ItemDto itemDto = ItemDto.builder().id(1L)
                .description("text")
                .available(true)
                .name("name")
                .build();
        when(itemService.getItemDtoById(any(Long.class), any(Long.class)))
                .thenReturn(itemDto);
        mvc.perform(get("/items/1")
                .content(objectMapper.writeValueAsString(itemDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        verify(itemService, times(1)).getItemDtoById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void getUserItems() {
        mockMvc.perform(get("/items")
                        .param("from", "1")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());
        doNothing().when(pageValidator).checkingPageableParams(anyInt(), anyInt());

        verify(itemService, times(1)).getItemsDtoByUserId(anyLong(), any(Pageable.class));

    }

    @SneakyThrows
    @Test
    void getItemsBySearch_whenCorrectPage_thenReturnOk() {
        mockMvc.perform(get("/items/search")
                        .param("text", "text")
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isOk());
        doNothing().when(pageValidator).checkingPageableParams(anyInt(), anyInt());

        verify(itemService, times(1)).getItemsDtoBySearch(anyString(), any(Pageable.class));
    }


    @SneakyThrows
    @Test
    void createCommentToItem() {
        long itemId = 1L;
        CommentDto commentToCreate = new CommentDto();
        when(itemService.addCommentToItem(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentToCreate);

        String result = mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1")
                        .content(objectMapper.writeValueAsString(commentToCreate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(commentToCreate), result);

    }
}

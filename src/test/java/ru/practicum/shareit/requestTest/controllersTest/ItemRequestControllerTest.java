package ru.practicum.shareit.requestTest.controllersTest;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.validator.PageValidatorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemRequestControllerTest {

    @Autowired
   private ObjectMapper objectMapper;

    @Autowired
   private MockMvc mockMvc;

    @MockBean
   private ItemRequestService itemRequestService;

    @MockBean
   private PageValidatorService pageValidator;

    @SneakyThrows
    @Test
    void addNewRequest() {
        ItemRequestDto itemRequestToCreate = new ItemRequestDto();
        when(itemRequestService.addNewRequest(any(ItemRequestDto.class), anyLong())).thenReturn(itemRequestToCreate);

        String result = mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(itemRequestToCreate))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemRequestToCreate), result);
    }

    @SneakyThrows
    @Test
    void getAllUserItemsWithResponses() {
        mockMvc.perform(get("/requests")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemRequestService, times(1)).getAllUserRequestsWithResponses(anyLong());
    }

    @SneakyThrows
    @Test
    void getAllCreatedRequests() {
        doNothing().when(pageValidator).checkingPageableParams(anyInt(), anyInt());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isOk());

        verify(itemRequestService, times(1)).getAllRequestsToResponse(anyLong(), any(Pageable.class));
    }

    @SneakyThrows
    @Test
    void getRequestById() {
        long requestId = 0L;
        mockMvc.perform(get("/requests/{requestId}", requestId)
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemRequestService, times(1)).getRequestById(anyLong(), anyLong());
    }
}

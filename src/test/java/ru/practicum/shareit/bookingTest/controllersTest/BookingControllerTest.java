package ru.practicum.shareit.bookingTest.controllersTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validator.PageValidatorService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {
    @Autowired
   private ObjectMapper objectMapper;

    @Autowired
   private MockMvc mockMvc;

    @MockBean
   private BookingService bookingService;

    @MockBean
   private PageValidatorService pageableValidator;

    @SneakyThrows
    @Test
    void createBooking() {
        BookingDto bookingToCreate = new BookingDto();
        when(bookingService.addBooking(any(BookingDto.class), anyLong()))
                .thenReturn(bookingToCreate);

        String result = mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingToCreate))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingToCreate), result);
    }

    @SneakyThrows
    @Test
    void approveBooking() {
        BookingDto bookingToCreate = new BookingDto();
        bookingToCreate.setId(1L);
        BookingDto updatedBooking = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingService.approveBooking(anyLong(), anyLong(), anyString())).thenReturn(updatedBooking);

        String result = mockMvc.perform(patch("/bookings/{bookingId}", bookingToCreate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingToCreate))
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(updatedBooking), result);
    }

    @SneakyThrows
    @Test
    void getAllBookingsForUser() {
        BookingDto booking = BookingDto.builder()
                .id(1L)
                .booker(new UserDto())
                .item(new ItemDto())
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        List<BookingDto> newBookingDto = new ArrayList<>();
        newBookingDto.add(booking);
        when(bookingService.getAllBookingsByUserId(anyLong(), anyString(), any()))
                .thenReturn(newBookingDto);
        mockMvc.perform(get("/bookings")
                        .param("from", "1")
                        .param("size", "1")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(newBookingDto)));
        doNothing().when(pageableValidator).checkingPageableParams(1, 1);

        verify(bookingService, times(1))
                .getAllBookingsByUserId(1L, "ALL", PageRequest.of(1, 1));
    }

    @SneakyThrows
    @Test
    void getAllBookingsForOwner() {
        BookingDto booking = BookingDto.builder()
                .id(1L)
                .booker(new UserDto())
                .item(new ItemDto())
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        List<BookingDto> newBookingDto = new ArrayList<>();
        newBookingDto.add(booking);
        when(bookingService.getAllBookingsByOwnerId(anyLong(), anyString(), any()))
                .thenReturn(newBookingDto);
        mockMvc.perform(get("/bookings/owner")
                        .param("from", "1")
                        .param("size", "1")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(newBookingDto)));
        doNothing().when(pageableValidator).checkingPageableParams(1, 1);

        verify(bookingService, times(1))
                .getAllBookingsByOwnerId(1L, "ALL", PageRequest.of(1, 1));
    }

    @SneakyThrows
    @Test
    void getInfoForBooking() {
        BookingDto booking = BookingDto.builder()
                .id(1L)
                .booker(new UserDto())
                .item(new ItemDto())
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        when(bookingService.getBookingInfo(anyLong(), anyLong()))
                .thenReturn(booking);
        long bookingId = 0L;
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(booking)));

        verify(bookingService, times(1)).getBookingInfo(anyLong(), anyLong());
    }
}

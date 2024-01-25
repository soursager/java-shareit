package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
public class BookingShortDto {
    private Long id;
    private Long bookerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private ItemDto item;
}

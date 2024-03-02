package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner() != null ? item.getOwner() : null)
                .request(item.getRequest())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId() != null ? itemDto.getId() : 0)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .request(itemDto.getRequest() != null ? itemDto.getRequest() : null)
                .build();
    }

    public static ItemDto toItemDtoWithBookings(Item item, List<BookingDto> bookings) {
        BookingDto lastBooking = null;
        BookingDto nextBooking = null;
        if (!bookings.isEmpty()) {
            lastBooking = bookings.stream()
                    .filter(x -> x.getStatus() != BookingStatus.REJECTED)
                    .filter(x -> x.getStatus() != BookingStatus.CANCELED)
                    .filter(x -> x.getStart().isBefore(LocalDateTime.now()))
                    .max(Comparator.comparing(BookingDto::getStart)).orElse(null);
            nextBooking = bookings.stream()
                    .filter(x -> x.getStatus() != BookingStatus.REJECTED)
                    .filter(x -> x.getStatus() != BookingStatus.CANCELED)
                    .filter(x -> x.getStart().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(BookingDto::getStart)).orElse(null);
        }
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(BookingMapper.toBookingShortDto(lastBooking))
                .nextBooking(BookingMapper.toBookingShortDto(nextBooking))
                .comments(new ArrayList<>())
                .build();
    }


    public static ItemDto toItemDtoWithBookingsAndComments(Item item, List<BookingDto> bookings,
                                                           List<CommentDto> comments) {
         ItemDto itemDto;
        if (bookings == null) {
            itemDto = toItemDto(item);
        } else {
            itemDto = toItemDtoWithBookings(item, bookings);
        }
        itemDto.setComments(comments != null ? comments : new ArrayList<>());
        return itemDto;
    }

    public static ItemDto toItemDtoWithComments(Item item, List<CommentDto> comments) {
        ItemDto itemDto = toItemDto(item);
        itemDto.setComments(comments != null ? comments : new ArrayList<>());
        return itemDto;
    }

    public static ItemDto toItemDtoWithRequestId(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner() != null ? item.getOwner() : null)
                .comments(new ArrayList<>())
                .requestId(item.getRequest().getId())
                .build();
    }

    public static Item toItemDbWithRequest(ItemDto itemDto, User user, ItemRequest request) {
        return Item.builder()
                .id(itemDto.getId() != null ? itemDto.getId() : 0)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .request(request)
                .build();
    }
}

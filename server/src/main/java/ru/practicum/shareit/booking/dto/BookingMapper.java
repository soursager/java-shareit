package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
        if (booking.getItem() != null) {
            ItemDto itemDto = ItemMapper.toItemDto(booking.getItem());
            bookingDto.setItem(itemDto);
            bookingDto.setItemId(itemDto.getId());
        }
        if (booking.getBooker() != null) {
            UserDto bookerDto = UserMapper.toUserDto(booking.getBooker());
            bookingDto.setBooker(bookerDto);
        }
        return bookingDto;
        }

    public static Booking toBookingDb(BookingDto bookingDto, Item item, User booker) {
        return Booking.builder()
                .id(bookingDto.getId() != null ? bookingDto.getId() : 0L)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .item(item)
                .booker(booker)
                .build();
    }

    public static Booking toBookingUpdate(BookingDto bookingDto, Booking booking) {
        Booking bookingUpdate = Booking.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(bookingDto.getStatus() != null ? bookingDto.getStatus() : booking.getStatus())
                .build();

        if (bookingDto.getItem() != null) {
            Item item = ItemMapper.toItem(bookingDto.getItem());
            bookingUpdate.setItem(item);
        }
        if (bookingDto.getBooker() != null) {
            User booker = UserMapper.toUser(bookingDto.getBooker());
            bookingUpdate.setBooker(booker);
        }
        return bookingUpdate;
    }

    public static BookingShortDto toBookingShortDto(BookingDto bookingDto) {
        if (bookingDto == null) {
            return null;
        }
        BookingShortDto bookingLiteDto = BookingShortDto.builder()
                .id(bookingDto.getId())
                .startTime(bookingDto.getStart())
                .endTime(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .build();
        if (bookingDto.getItem() != null) {
            ItemDto item = bookingDto.getItem();
            bookingLiteDto.setItem(item);
        }
        if (bookingDto.getBooker() != null) {
            User booker = UserMapper.toUser(bookingDto.getBooker());
            bookingLiteDto.setBookerId(booker.getId());
        }
        return bookingLiteDto;
    }
}

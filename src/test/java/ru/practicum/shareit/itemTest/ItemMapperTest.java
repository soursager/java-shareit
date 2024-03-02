package ru.practicum.shareit.itemTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemMapperTest {

    private static final LocalDateTime START_TIME = LocalDateTime.now();
    private Item item;
    private User owner;
    private ItemDto itemDto;
    private ItemRequest request;
    private BookingDto bookingDto;
    private CommentDto commentDto;

    @BeforeEach
    public void fillData() {
        item = Item.builder()
                .available(true)
                .id(1L)
                .name("name")
                .description("desc")
                .build();
        owner = User.builder()
                .name("name")
                .id(1L)
                .build();
        itemDto = ItemDto.builder()
                .id(1L)
                .description("text")
                .available(true)
                .name("name")
                .build();
        request = ItemRequest.builder()
                .requester(owner)
                .description("desc")
                .id(1L)
                .build();
        bookingDto = BookingDto.builder()
                .start(START_TIME)
                .end(START_TIME.plusDays(1))
                .status(BookingStatus.WAITING)
                .build();
        commentDto = CommentDto.builder()
                .authorName("Bob")
                .item(itemDto)
                .text("text")
                .id(1L)
                .build();
    }

    @Test
    void toItemDto() {
        ItemDto actual = ItemMapper.toItemDto(item);
        assertEquals(actual.getId(), item.getId());
        assertEquals(actual.getName(), item.getName());
        assertEquals(actual.getDescription(), item.getDescription());
        assertEquals(actual.getAvailable(), item.getAvailable());

    }

    @Test
    void toItemDtoWithRequestId() {
        item.setRequest(request);
        ItemDto actual = ItemMapper.toItemDtoWithRequestId(item);
        assertEquals(actual.getId(), item.getId());
        assertEquals(actual.getName(), item.getName());
        assertEquals(actual.getRequestId(), item.getRequest().getId());
    }

    @Test
    void toItem() {
        Item actual = ItemMapper.toItem(itemDto);
        assertEquals(actual.getId(), itemDto.getId());
        assertEquals(actual.getName(), itemDto.getName());
        assertEquals(actual.getDescription(), itemDto.getDescription());
        assertEquals(actual.getAvailable(), itemDto.getAvailable());
    }

    @Test
    void toItemDbWithRequest() {
        Item actual = ItemMapper.toItemDbWithRequest(itemDto, owner, request);
        assertEquals(actual.getName(), itemDto.getName());
        assertEquals(actual.getOwner().getId(), owner.getId());
        assertEquals(actual.getRequest().getId(), request.getId());
    }

    @Test
    void toItemDtoWithBookings() {
        ItemDto actual = ItemMapper.toItemDtoWithBookings(item, List.of(bookingDto));
        assertEquals(actual.getAvailable(), item.getAvailable());
        assertEquals(actual.getLastBooking().getStatus(), bookingDto.getStatus());
    }

    @Test
    void toItemDtoWithBookingsAndComments() {
        ItemDto actual = ItemMapper.toItemDtoWithBookingsAndComments(item, List.of(bookingDto), List.of(commentDto));
        assertEquals(actual.getLastBooking().getStatus(), bookingDto.getStatus());
        assertEquals(actual.getAvailable(),item.getAvailable());
        assertEquals(actual.getComments().size(), 1);
    }

    @Test
    void toItemDtoWithComments() {
        ItemDto actual = ItemMapper.toItemDtoWithComments(item, List.of(commentDto));
        assertEquals(actual.getAvailable(),item.getAvailable());
        assertEquals(actual.getComments().size(), 1);
    }
}

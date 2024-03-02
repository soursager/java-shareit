package ru.practicum.shareit.itemTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.ItemValidator;
import ru.practicum.shareit.validator.UserValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.item.dto.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.dto.ItemMapper.*;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    private User owner;
    private ItemRequest request;
    private Item item;

    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    RequestRepository itemRequestRepository;
    @Mock
    UserValidator userValidator;
    @Mock
    ItemValidator itemValidator;
    @InjectMocks
    ItemServiceImpl itemService;

    @BeforeEach
    public void fillData() {

        owner = User.builder()
                .id(1L)
                .name("name")
                .email("email@mai.ru")
                .build();

        request = ItemRequest.builder()
                .id(1L)
                .description("need")
                .build();

        item = Item.builder()
                .id(1L)
                .available(true)
                .description("desc")
                .name("name")
                .owner(owner)
                .request(request)
                .build();
    }

    @Test
    void create_whenAllDataIsCorrect_thenReturnItemWithoutRequest() {
        when(userValidator.returnUserIfExists(1L))
                .thenReturn(owner);
        when(itemRepository.save(item))
                .thenReturn(item);

        ItemDto actualItem = itemService.createItemDto(toItemDto(item), 1L);

        assertEquals(item.getName(), actualItem.getName());
    }

    @Test
    void create_whenAllDataIsCorrect_thenReturnItemWithRequest() {
        when(userValidator.returnUserIfExists(1L)).thenReturn(owner);
        when(itemRepository.save(item))
                .thenReturn(item);
        when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        ItemDto actualItem = itemService.createItemDto(toItemDtoWithRequestId(item), 1L);

        assertEquals(item.getName(), actualItem.getName());
        assertEquals(item.getRequest().getId(), actualItem.getRequestId());
    }

    @Test
    void create_whenDataIsIncorrect_thenThrowEmptyFieldExceptionException() {
        when(userValidator.returnUserIfExists(1L)).thenReturn(owner);
        when(itemRepository.save(item))
                .thenThrow(new DataNotFoundException("Ошибка!"));

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.createItemDto(toItemDto(item), 1L));
        assertEquals(dataNotFoundException.getMessage(), "Ошибка!");
    }

    @Test
    void update_whenUserAndItemExist_thenReturnItem() {
        when(itemRepository.save(item)).thenReturn(item);
        when(itemValidator.validateItemId(1L)).thenReturn(item);
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        ItemDto actualItem = itemService.updateItemDto(toItemDto(item), 1L, 1L);

        assertEquals(item.getName(), actualItem.getName());
        verify(userValidator, times(0)).checkingUserId(1L);
    }

    @Test
    void update_whenUserNotExist_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователя не существует!"))
                .when(userValidator).checkingUserIdAndNotReturns(1L);

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.updateItemDto(toItemDto(item), 1L, 1L));

        assertEquals(dataNotFoundException.getMessage(), "Пользователя не существует!");
    }

    @Test
    void update_whenItemNotExists_thenThrowEntityNotFoundException() {
        long itemId = 1L;
        Item expectedItem = new Item();
        expectedItem.setId(itemId);
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemValidator.validateItemId(itemId))
                .thenThrow(new DataNotFoundException("Несуществующая вещь!"));

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.updateItemDto(toItemDto(expectedItem), 1L, 1L));

        assertEquals(dataNotFoundException.getMessage(), "Несуществующая вещь!");
    }

    @Test
    void getItemById_whenItemAndUserExists_thenReturnItem() {
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        ItemDto actualItem = itemService.getItemDtoById(1L, 1L);
        assertEquals(item.getId(), actualItem.getId());
    }

    @Test
    void getItemById_UserNotExists_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователь не существует")).when(userValidator).checkingUserIdAndNotReturns(1L);

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.getItemDtoById(1L, 1L));

        assertEquals(entityNotFoundException.getMessage(), "Пользователь не существует");
    }

    @Test
    void getItemById_ItemNotExists_thenThrowEntityNotFoundException() {
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findById(1L))
                .thenThrow(new DataNotFoundException("Несуществующая вещь!"));

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.getItemDtoById(1L, 1L));

        assertEquals(entityNotFoundException.getMessage(), "Несуществующая вещь!");
    }

    @Test
    void getItemById_whenItemAndUserExists_thenReturnItemWithBookings() {
        LocalDateTime nextBooking = LocalDateTime.now();
        List<Booking> bookings = List.of(Booking.builder()
                .item(item)
                .booker(owner)
                .start(nextBooking)
                .end(null)
                .status(BookingStatus.APPROVED)
                .build());
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItem_Owner_Id(1L)).thenReturn(bookings);

        ItemDto actualItem = itemService.getItemDtoById(1L, 1L);

        assertEquals(item.getId(), actualItem.getId());
        assertEquals(actualItem.getLastBooking().getBookerId(), 1L);
    }

    @Test
    void getItemById_whenItemAndUserExists_thenReturnItemWithComments() {
        List<Comment> comments = List.of(Comment.builder()
                .text("Text")
                .item(item)
                .created(LocalDateTime.now())
                .author(owner)
                .build());
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItem_Id(1L)).thenReturn(comments);

        ItemDto actualItem = itemService.getItemDtoById(1L, 1L);

        assertEquals(item.getId(), actualItem.getId());
        assertEquals(actualItem.getComments().size(), 1);
    }

    @Test
    void getItemById_whenItemAndUserExists_thenReturnItemWithBookingsAndComments() {
        LocalDateTime nextBooking = LocalDateTime.now();
        List<Booking> bookings = List.of(Booking.builder()
                .item(item)
                .booker(owner)
                .start(nextBooking)
                .end(null)
                .status(BookingStatus.APPROVED)
                .build());
        List<Comment> comments = List.of(Comment.builder()
                .text("Text")
                .item(item)
                .created(LocalDateTime.now())
                .author(owner)
                .build());
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItem_Owner_Id(1L)).thenReturn(bookings);
        when(commentRepository.findAllByItem_Id(1L)).thenReturn(comments);

        ItemDto actualItem = itemService.getItemDtoById(1L, 1L);

        assertEquals(item.getId(), actualItem.getId());
        assertEquals(actualItem.getComments().size(), 1);
        assertEquals(actualItem.getLastBooking().getBookerId(), 1L);
    }

    @Test
    void getItemsByUserId_whenUserExists_thenReturnUserItems() {
        Pageable pageForItems = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findByOwner_Id(1L, pageForItems)).thenReturn(List.of(new Item()));

        Collection<ItemDto> userItems = itemService.getItemsDtoByUserId(1L, pageForItems);

        assertEquals(userItems.size(), 1);
    }

    @Test
    void getItemsByUserId_whenUserNotExists_thenThrowEntityNotFoundException() {
        Pageable pageForItems = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        doThrow(new DataNotFoundException("Пользователь не существует")).when(userValidator).checkingUserIdAndNotReturns(1L);

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.getItemsDtoByUserId(1L, pageForItems));

        assertEquals(entityNotFoundException.getMessage(), "Пользователь не существует");
    }

    @Test
    void getItemsByUserId_whenUserExists_thenReturnUserItemsWithComments() {
        Pageable pageForItems = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageForComments = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<Comment> comments = List.of(Comment.builder()
                .text("Text")
                .item(item)
                .created(LocalDateTime.now())
                .build());
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findByOwner_Id(1L, pageForItems)).thenReturn(List.of(item));
        when(commentRepository.findAllByItemsUser_Id(1L, pageForComments)).thenReturn(comments);

        Collection<ItemDto> userItems = itemService.getItemsDtoByUserId(1L, pageForItems);
        List<ItemDto> items = new ArrayList<>(userItems);

        assertEquals(userItems.size(), 1);
        assertEquals(items.get(0).getComments().size(), 1);
    }

    @Test
    void getItemsByUserId_whenUserExists_thenReturnUserItemsWithBookings() {
        LocalDateTime start = LocalDateTime.now();
        Pageable pageForItems = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        List<Booking> bookings = List.of(Booking.builder()
                .item(item)
                .start(start)
                .end(null)
                .status(BookingStatus.APPROVED)
                .build());
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findByOwner_Id(1L, pageForItems)).thenReturn(List.of(item));
        when(bookingRepository.findAllByItem_Owner_Id(1L)).thenReturn(bookings);

        Collection<ItemDto> userItems = itemService.getItemsDtoByUserId(1L, pageForItems);
        List<ItemDto> items = new ArrayList<>(userItems);

        assertEquals(userItems.size(), 1);
        assertEquals(items.get(0).getLastBooking().getStartTime(), start);
    }

    @Test
    void getItemsByUserId_whenUserExists_thenReturnUserItemsWithBookingsAndComments() {
        LocalDateTime start = LocalDateTime.now();
        Pageable pageForItems = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        List<Booking> bookings = List.of(Booking.builder()
                .item(item)
                .start(start)
                .end(null)
                .status(BookingStatus.APPROVED)
                .build());
        Pageable pageForComments = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<Comment> comments = List.of(Comment.builder()
                .text("Text")
                .item(item)
                .created(LocalDateTime.now())
                .build());
        doNothing().when(userValidator).checkingUserIdAndNotReturns(1L);
        when(itemRepository.findByOwner_Id(1L, pageForItems)).thenReturn(List.of(item));
        when(bookingRepository.findAllByItem_Owner_Id(1L)).thenReturn(bookings);
        when(commentRepository.findAllByItemsUser_Id(1L, pageForComments)).thenReturn(comments);

        Collection<ItemDto> userItems = itemService.getItemsDtoByUserId(1L, pageForItems);
        List<ItemDto> items = new ArrayList<>(userItems);

        assertEquals(userItems.size(), 1);
        assertEquals(items.get(0).getLastBooking().getStartTime(), start);
        assertEquals(items.get(0).getComments().size(), 1);
    }

    @Test
    void getItemsBySearch_whenItemsAvailableTrue_thenReturnItems() {
        Item item = Item.builder()
                .name("name")
                .description("desc")
                .available(true)
                .build();
        Pageable page = PageRequest.of(0, 10);
        String search = "DesC";
        when(itemRepository.getItemsBySearchQuery("DesC", page)).thenReturn(List.of(item));

        Collection<ItemDto> items = itemService.getItemsDtoBySearch(search, page);
        List<ItemDto> itemsList = new ArrayList<>(items);

        assertEquals(itemsList.size(), 1);
        assertEquals(itemsList.get(0).getName(), "name");
    }

    @Test
    void getItemsBySearch_whenItemsAvailableFalse_thenReturnEmptyList() {
        Pageable page = PageRequest.of(0, 10);
        String search = "DesC";
        when(itemRepository.getItemsBySearchQuery("DesC", page)).thenReturn(new ArrayList<>());

        Collection<ItemDto> items = itemService.getItemsDtoBySearch(search, page);
        List<ItemDto> itemsList = new ArrayList<>(items);

        assertEquals(itemsList.size(), 0);
    }


    @Test
    void addCommentToItem_whenItemAndUserExistAndCommentDataCorrect_thenReturnComment() {
        List<Booking> bookings = List.of(Booking.builder()
                .item(null)
                .start(null)
                .end(null)
                .status(BookingStatus.APPROVED)
                .build());
        Comment expectedComment = Comment.builder().id(1L).text("text").build();
        when(userValidator.checkingUserId(1L)).thenReturn(toUserDto(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByUserIdAndItemIdAndEndDateIsPassed(any(Long.class), any(Long.class),
                any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(commentRepository.save(any(Comment.class))).thenReturn(expectedComment);

        CommentDto actualComment = itemService.addCommentToItem(1L, 1L, toCommentDto(expectedComment));

        assertEquals(toCommentDto(expectedComment), actualComment);
    }

    @Test
    void addCommentToItem_whenItemAndUserExistAndCommentDataInCorrect_thenThrowIncorrectDataException() {
        Comment comment = Comment.builder().build();
        doThrow(new DataValidationException("Комментарий не может быть пустым")).when(itemValidator)
                .validateCommentData(toCommentDto(comment));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> itemService.addCommentToItem(1L, 1L, toCommentDto(comment)));

        assertEquals(dataValidationException.getMessage(), "Комментарий не может быть пустым");
    }

    @Test
    void addCommentToItem_whenUserNotExists_thenThrowEntityNotFoundException() {
        Comment comment = Comment.builder().build();
        when(userValidator.checkingUserId(1L)).thenThrow(new DataNotFoundException("Пользователя не существует!"));

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> itemService.addCommentToItem(1L, 1L, toCommentDto(comment)));

        assertEquals(entityNotFoundException.getMessage(), "Пользователя не существует!");
    }

    @Test
    void addCommentToItem_whenUserBookingsAreEmpty_thenThrowIncorrectDataException() {
        Comment expectedComment = Comment.builder()
                .id(1L)
                .text("text")
                .created(LocalDateTime.now())
                .build();
        when(bookingRepository.findAllByUserIdAndItemIdAndEndDateIsPassed(any(Long.class), any(Long.class),
                any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> itemService.addCommentToItem(1L, 1L, toCommentDto(expectedComment)));

        assertEquals(dataValidationException.getMessage(), "Данный пользователь не бронировал вещь");
    }
}
package ru.practicum.shareit.bookingTest.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.BookingValidatorService;
import ru.practicum.shareit.validator.ItemValidatorService;
import ru.practicum.shareit.validator.UserValidatorService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.dto.BookingMapper.toBookingDto;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    private static final Pageable PAGE_BOOKINGS = PageRequest.of(0, 10,
            Sort.by("start").descending());

    private User owner;
    private Item ownerItem;
    private Booking booking;
    private UserDto userDto;

    @Mock
   private ItemRepository itemRepository;

    @Mock
   private BookingRepository bookingRepository;

    @Mock
   private UserValidatorService userValidator;

    @Mock
   private ItemValidatorService itemValidator;

    @Mock
   private BookingValidatorService bookingValidator;

    @InjectMocks
   private BookingServiceImpl bookingService;

    @BeforeEach
    public void fillData() {
        owner = User.builder()
                .id(1L)
                .name("name")
                .email("email@mai.ru")
                .build();

        ownerItem = Item.builder()
                .id(1L)
                .description("desc")
                .name("name")
                .owner(owner)
                .available(true)
                .build();

        booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .item(ownerItem)
                .booker(new User())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        userDto = toUserDto(owner);
    }

    @Test
    void addBooking_whenUserAndItemExistAndAllDataIsCorrect_thenReturnBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(itemValidator.validateItemId(anyLong())).thenReturn(ownerItem);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto expectedBooking = bookingService.addBooking(toBookingDto(booking), 2L);

        assertEquals(expectedBooking, toBookingDto(booking));
    }

    @Test
    void addBooking_whenItemOwnerIdEqualsBookerId_thenThrowEntityNotFoundException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(itemValidator.validateItemId(anyLong())).thenReturn(ownerItem);

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.addBooking(toBookingDto(booking), 1L));

        assertEquals(entityNotFoundException.getMessage(),
                "Владелец предмета не может бронировать свой предмет");
    }

    @Test
    void addBooking_whenItemIsNotAvailable_thenThrowIncorrectDataException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(itemValidator.validateItemId(anyLong())).thenReturn(ownerItem);
        ownerItem.setAvailable(false);

        DataValidationException incorrectDataException = assertThrows(DataValidationException.class,
                () -> bookingService.addBooking(toBookingDto(booking), 2L));

        assertEquals(incorrectDataException.getMessage(), "В данный момент товар недоступен!");
    }

    @Test
    void addBooking_whenBookingDatesAreNull_thenThrowIncorrectDataException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(itemValidator.validateItemId(anyLong())).thenReturn(ownerItem);
        booking.setStart(null);

        DataValidationException incorrectDataException = assertThrows(DataValidationException.class,
                () -> bookingService.addBooking(toBookingDto(booking), 2L));

        assertEquals(incorrectDataException.getMessage(), "Неверно введены даты!");
    }

    @Test
    void addBooking_whenBookingDatesAreEquals_thenThrowIncorrectDataException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(itemValidator.validateItemId(anyLong())).thenReturn(ownerItem);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now());

        DataValidationException incorrectDataException = assertThrows(DataValidationException.class,
                () -> bookingService.addBooking(toBookingDto(booking), 2L));

        assertEquals(incorrectDataException.getMessage(), "Неверно введены даты!");
    }

    @Test
    void addBooking_whenUserNotFound_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователь не найден!"))
                .when(userValidator).checkingUserId(anyLong());

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.addBooking(toBookingDto(booking), 2L));

        assertEquals(entityNotFoundException.getMessage(), "Пользователь не найден!");
    }

    @Test
    void addBooking_whenItemNotFound_thenThrowEntityNotFoundException() {
        when(userValidator.checkingUserId(anyLong()))
                .thenReturn(toUserDto(owner));
        when(itemValidator.validateItemId(anyLong()))
                .thenThrow(new DataNotFoundException("Предмет не найден!"));

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.addBooking(toBookingDto(booking), 2L));

        assertEquals(entityNotFoundException.getMessage(), "Предмет не найден!");
    }

    @Test
    void approveBooking_whenUserAndItemExistAndAllDataCorrect_thenReturnBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(bookingValidator.validateBooking(anyLong())).thenReturn(booking);

        BookingDto actualBooking = bookingService.approveBooking(1L, 1L, "true");

        assertEquals(actualBooking.getId(), booking.getId());
        assertEquals(actualBooking.getStart(), booking.getStart());
    }

    @Test
    void approveBooking_whenUserAndItemExistAnApproveIncorrect_thenThrowIncorrectDataException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(bookingValidator.validateBooking(anyLong())).thenReturn(booking);

        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> bookingService.approveBooking(1L, 1L, "incorrect"));

        assertEquals(exception.getMessage(), "Некорректный метод");
    }

    @Test
    void approveBooking_whenUserAndItemExistAndUserIsNotOwner_thenThrowEntityNotFoundException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(bookingValidator.validateBooking(anyLong())).thenReturn(booking);

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingService.approveBooking(1L, 2L, "true"));

        assertEquals(exception.getMessage(), "Пользователь под номером: 2 не является владельцем!");
    }

    @Test
    void approveBooking_whenUserAndItemExistAndStatusIsApprove_thenThrowIncorrectDataException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(bookingValidator.validateBooking(anyLong())).thenReturn(booking);
        booking.setStatus(BookingStatus.APPROVED);

        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> bookingService.approveBooking(1L, 1L, "true"));

        assertEquals(exception.getMessage(), "Статус APPROVED");
    }

    @Test
    void approveBooking_whenUserNotFound_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователь не найден!"))
                .when(userValidator).checkingUserId(anyLong());

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.approveBooking(1L, 1L, "true"));

        assertEquals(dataNotFoundException.getMessage(), "Пользователь не найден!");
    }

    @Test
    void approveBooking_whenBookingNotFound_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Бронирование не найдено!"))
                .when(bookingValidator).validateBooking(anyLong());

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> bookingService.approveBooking(1L, 1L, "true"));

        assertEquals(entityNotFoundException.getMessage(), "Бронирование не найдено!");
    }

    @Test
    void getBookingInfo_whenUserIsOwnerAndBookingExist_thenReturnBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(bookingValidator.validateBookingDto(anyLong())).thenReturn(toBookingDto(booking));

        BookingDto actualBooking = bookingService.getBookingInfo(1L, 1L);

        assertEquals(actualBooking.getStatus(), booking.getStatus());
    }

    @Test
    void getBookingInfo_whenUserIsNotOwner_thenThrowEntityNotFoundException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(bookingValidator.validateBookingDto(anyLong())).thenReturn(toBookingDto(booking));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingService.getBookingInfo(1L, 2L));

        assertEquals(exception.getMessage(), "Пользователь под номером: 2 не является владельцем!");
    }

    @Test
    void getBookingInfo_whenBookingNotFound_thenThrowEntityNotFoundException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(toUserDto(owner));
        when(bookingValidator.validateBookingDto(anyLong()))
                .thenThrow(new DataNotFoundException("Бронирование не найдено!"));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingService.getBookingInfo(4L, 1L));

        assertEquals(exception.getMessage(), "Бронирование не найдено!");
    }

    @Test
    void getAllBookingsByUserId_whenUserAndStateExist_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(bookingRepository.findAllByBookerIdAndCurrentStatus(anyLong(), any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByUserId(1L, "CURRENT",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);
        verify(bookingRepository, times(0)).findAllByBookerIdAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndPastStatus(any(), any(), any());
        verify(bookingRepository, times(0)).findAllByBooker_Id(any(), any());
    }

    @Test
    void getAllBookingsByUserId_whenUserAndStateExistAndWaiting_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(bookingRepository.findAllByBookerIdAndWaitingStatus(anyLong(), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByUserId(1L, "WAITING",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);

        verify(bookingRepository, times(0)).findAllByBookerIdAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndPastStatus(any(), any(), any());
        verify(bookingRepository, times(0)).findAllByBooker_Id(any(), any());
    }

    @Test
    void getAllBookingsByUserId_whenUserAndStateExistAndFuture_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(bookingRepository.findAllByBookerIdAndFutureStatus(anyLong(), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByUserId(1L, "FUTURE",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);

        verify(bookingRepository, times(0)).findAllByBookerIdAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndPastStatus(any(), any(), any());
        verify(bookingRepository, times(0)).findAllByBooker_Id(any(), any());
    }

    @Test
    void getAllBookingsByUserId_whenUserAndStateExistAndRejected_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(bookingRepository.findAllByBookerIdAndRejectedStatus(anyLong(), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByUserId(1L, "REJECTED",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);

        verify(bookingRepository, times(0)).findAllByBookerIdAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndPastStatus(any(), any(), any());
        verify(bookingRepository, times(0)).findAllByBooker_Id(any(), any());
    }

    @Test
    void getAllBookingsByUserId_whenUserAndStateExistAndPast_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(bookingRepository.findAllByBookerIdAndPastStatus(anyLong(), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByUserId(1L, "PAST",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);

        verify(bookingRepository, times(0)).findAllByBookerIdAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBooker_Id(any(), any());
    }

    @Test
    void getAllBookingsByUserId_whenUserAndStateExistAndAll_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(bookingRepository.findAllByBooker_Id(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByUserId(1L, "ALL",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);

        verify(bookingRepository, times(0)).findAllByBookerIdAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByBookerIdAndPastStatus(any(), any(), any());
    }

    @Test
    void getAllBookingsByUserId_whenUserNotExists_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователь не найден!"))
                .when(userValidator).checkingUserId(anyLong());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingService.getAllBookingsByUserId(1L, "CURRENT", PAGE_BOOKINGS));

        assertEquals(exception.getMessage(), "Пользователь не найден!");
    }

    @Test
    void getAllBookingsByUserId_whenUserAndStateIncorrect_thenThrowUnsupportedStatusException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doThrow(new UnsupportedStatusException("Статуса не существует!"))
                .when(bookingValidator).checkingBookingState(anyString());

        UnsupportedStatusException exception = assertThrows(UnsupportedStatusException.class,
                () -> bookingService.getAllBookingsByUserId(1L, "NOT", PAGE_BOOKINGS));

        assertEquals(exception.getMessage(), "Статуса не существует!");
    }

    @Test
    void getAllBookingsByOwnerId_whenUserAndBookingAndItemExist_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(itemRepository.findByOwner_Id_WithoutPageable(anyLong())).thenReturn(List.of(ownerItem));
        when(bookingRepository.findAllByOwnerItemsAndCurrentStatus(anyList(), any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByOwnerId(1L, "CURRENT",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndPastStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItems(any(), any());
    }

    @Test
    void getAllBookingsByOwnerId_whenUserAndBookingAndItemExistWaiting_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(itemRepository.findByOwner_Id_WithoutPageable(anyLong())).thenReturn(List.of(ownerItem));
        when(bookingRepository.findAllByOwnerItemsAndWaitingStatus(anyList(), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByOwnerId(1L, "WAITING",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndPastStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItems(any(), any());
    }

    @Test
    void getAllBookingsByOwnerId_whenUserAndBookingAndItemExistFuture_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(itemRepository.findByOwner_Id_WithoutPageable(anyLong())).thenReturn(List.of(ownerItem));
        when(bookingRepository.findAllByOwnerItemsAndFutureStatus(anyList(), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByOwnerId(1L, "FUTURE",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndPastStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItems(any(), any());
    }

    @Test
    void getAllBookingsByOwnerId_whenUserAndBookingAndItemExistRejected_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(itemRepository.findByOwner_Id_WithoutPageable(anyLong())).thenReturn(List.of(ownerItem));
        when(bookingRepository.findAllByOwnerItemsAndRejectedStatus(anyList(), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByOwnerId(1L, "REJECTED",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndPastStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItems(any(), any());
    }

    @Test
    void getAllBookingsByOwnerId_whenUserAndBookingAndItemExistPast_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(itemRepository.findByOwner_Id_WithoutPageable(anyLong())).thenReturn(List.of(ownerItem));
        when(bookingRepository.findAllByOwnerItemsAndPastStatus(anyList(), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByOwnerId(1L, "PAST",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItems(any(), any());
    }

    @Test
    void getAllBookingsByOwnerId_whenUserAndBookingAndItemExistAll_thenReturnListOfBooking() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doNothing().when(bookingValidator).checkingBookingState(anyString());
        when(itemRepository.findByOwner_Id_WithoutPageable(anyLong())).thenReturn(List.of(ownerItem));
        when(bookingRepository.findAllByOwnerItems(any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> actualBookings = bookingService.getAllBookingsByOwnerId(1L, "ALL",
                PAGE_BOOKINGS);

        assertEquals(actualBookings.size(), 1);
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndCurrentStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndWaitingStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndFutureStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndRejectedStatus(any(), any(),
                any());
        verify(bookingRepository, times(0)).findAllByOwnerItemsAndPastStatus(any(), any(),
                any());
    }

    @Test
    void getAllBookingsByOwnerId_whenUserNotExists_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователь не найден!"))
                .when(userValidator).checkingUserId(anyLong());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingService.getAllBookingsByOwnerId(1L, "CURRENT", PAGE_BOOKINGS));

        assertEquals(exception.getMessage(), "Пользователь не найден!");
    }

    @Test
    void getAllBookingsByOwnerId_whenUserAndStateIncorrect_thenThrowUnsupportedStatusException() {
        when(userValidator.checkingUserId(anyLong())).thenReturn(userDto);
        doThrow(new UnsupportedStatusException("state"))
                .when(bookingValidator).checkingBookingState(anyString());

        UnsupportedStatusException exception = assertThrows(UnsupportedStatusException.class,
                () -> bookingService.getAllBookingsByOwnerId(1L, "NOT", PAGE_BOOKINGS));

        assertEquals(exception.getMessage(), "state");
    }
}
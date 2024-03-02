package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.BookingValidator;
import ru.practicum.shareit.validator.ItemValidator;
import ru.practicum.shareit.validator.UserValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserValidator userValidator;
    private final ItemValidator itemValidator;
    private final BookingValidator bookingValidator;

    @Transactional
    @Override
    public BookingDto addBooking(BookingDto bookingDto, Long bookerId) {
        UserDto userFromDb = userValidator.checkingUserId(bookerId);
        Item itemFromDb = itemValidator.validateItemId(bookingDto.getItemId());
        if (Objects.equals(itemFromDb.getOwner().getId(), bookerId)) {
            throw new DataNotFoundException("Владелец предмета не может бронировать свой предмет");
        }
        if (!itemFromDb.getAvailable()) {
            throw new DataValidationException("В данный момент товар недоступен!");
        }
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new DataValidationException("Неверно введены даты!");
        }
        if (checkingTheCreationTime(bookingDto)) {
            throw new DataValidationException("Неверно введены даты!");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        User newUser = UserMapper.toUser(userFromDb);
        Booking newBooking = bookingRepository.save(BookingMapper.toBookingDb(bookingDto, itemFromDb,
                newUser));
        return BookingMapper.toBookingDto(newBooking);
    }

    @Transactional
    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, String approve) {
        userValidator.checkingUserId(ownerId);
        Booking bookingFromDb = bookingValidator.validateAndReturnsBooking(bookingId);
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingFromDb);

        if (!Objects.equals(bookingDto.getItem().getOwner().getId(), ownerId)) {
            throw new DataNotFoundException("Пользователь под номером: " + ownerId + " не является владельцем!");
        }

        if (approve.equalsIgnoreCase("true")) {
            if (bookingDto.getStatus().equals(BookingStatus.APPROVED)) {
                throw new DataValidationException("Статус APPROVED");
            }
            bookingDto.setStatus(BookingStatus.APPROVED);
        } else if (approve.equalsIgnoreCase("false")) {
            bookingDto.setStatus(BookingStatus.REJECTED);
        } else {
            throw new DataValidationException("Некорректный метод");
        }
        Booking bookingToUpdate = BookingMapper.toBookingUpdate(bookingDto, bookingFromDb);
        bookingRepository.save(bookingToUpdate);
        return BookingMapper.toBookingDto(bookingToUpdate);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getBookingInfo(Long bookingId, Long userId) {
        userValidator.checkingUserId(userId);
        BookingDto bookingDto = bookingValidator.validateAndReturnsBookingDto(bookingId);
        if (!Objects.equals(bookingDto.getItem().getOwner().getId(), userId)
                && !Objects.equals(bookingDto.getBooker().getId(), userId)) {
            throw new DataNotFoundException("Пользователь под номером: " + userId + " не является владельцем!");
        }
        return bookingDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllBookingsByUserId(Long userId, String state, Pageable page) {
        userValidator.checkingUserId(userId);
        bookingValidator.checkingBookingState(state);
        Pageable bookingPage = PageRequest.of(page.getPageNumber(), page.getPageSize(), SORT_BY_START_DESC);
        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "WAITING": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndWaitingStatus(userId,
                        BookingStatus.WAITING, bookingPage));
                break;
            }
            case "REJECTED": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndRejectedStatus(userId,
                        List.of(BookingStatus.REJECTED, BookingStatus.CANCELED), bookingPage));
                break;
            }
            case "CURRENT": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndCurrentStatus(userId,
                        LocalDateTime.now(), bookingPage));
                break;
            }
            case "FUTURE": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndFutureStatus(userId,
                        LocalDateTime.now(), bookingPage));
                break;
            }
            case "PAST": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndPastStatus(userId,
                        LocalDateTime.now(), bookingPage));
                break;
            }
            case "ALL": {
                bookings = new ArrayList<>(bookingRepository.findAllByBooker_Id(userId, bookingPage));
                break;
            }
            default:
                bookings = new ArrayList<>();
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllBookingsByOwnerId(Long ownerId, String state, Pageable page) {
        userValidator.checkingUserId(ownerId);
        bookingValidator.checkingBookingState(state);
        Pageable allBookingsForOwner = PageRequest.of(page.getPageNumber(), page.getPageSize(), SORT_BY_START_DESC);
        List<Long> userItemsIds = itemRepository.findByOwner_Id_WithoutPageable(ownerId)
                .stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        if (userItemsIds.isEmpty()) {
            throw new DataValidationException("У данного пользователя нет предметов!");
        }
        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "WAITING": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndWaitingStatus(userItemsIds,
                        BookingStatus.WAITING, allBookingsForOwner));
                break;
            }
            case "REJECTED": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndRejectedStatus(userItemsIds,
                        List.of(BookingStatus.REJECTED, BookingStatus.CANCELED), allBookingsForOwner));
                break;
            }
            case "CURRENT": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndCurrentStatus(userItemsIds,
                        LocalDateTime.now(), allBookingsForOwner));
                break;
            }
            case "FUTURE": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndFutureStatus(userItemsIds,
                        LocalDateTime.now(), allBookingsForOwner));
                break;
            }
            case "PAST": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndPastStatus(userItemsIds,
                        LocalDateTime.now(), allBookingsForOwner));
                break;
            }
            case "ALL": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItems(userItemsIds, allBookingsForOwner));
                break;
            }
            default:
                bookings = new ArrayList<>();
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private boolean checkingTheCreationTime(BookingDto bookingDto) {
        boolean endIsBeforeStart = bookingDto.getEnd().isBefore(bookingDto.getStart());
        boolean startIsEqualEnd = bookingDto.getStart().isEqual(bookingDto.getEnd());
        boolean endIsBeforeNow = bookingDto.getEnd().isBefore(LocalDateTime.now());
        boolean startIsBeforeNow = bookingDto.getStart().isBefore(LocalDateTime.now());
        return endIsBeforeStart || startIsEqualEnd || endIsBeforeNow || startIsBeforeNow;
    }
}

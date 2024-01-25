package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Override
    public BookingDto addBooking(BookingDto bookingDto, Long bookerId) {
        UserDto userFromDb = checkingUserId(bookerId);
        Item itemFromDb = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new DataNotFoundException("Такого предмета не существует!"));

        if (Objects.equals(itemFromDb.getOwner().getId(), bookerId)) {
            throw new DataNotFoundException("Владелец предмета не может бронировать свой предмет");
        }
        if (!itemFromDb.getAvailable()) {
            throw new DataValidationException("В данный момент товар недоступен!");
        }
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new DataValidationException("Неверно введены даты!");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) || bookingDto.getStart().isEqual(bookingDto.getEnd())
                || bookingDto.getEnd().isBefore(LocalDateTime.now())
                || bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new DataValidationException("Неверно введены даты!");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(BookingMapper.toBookingDb(bookingDto, itemFromDb,
                UserMapper.toUser(userFromDb))));
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, String approve) {
        checkingUserId(ownerId);
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("There is no Booking with Id: " + bookingId)));

        if (!Objects.equals(bookingDto.getItem().getOwner().getId(), ownerId)) {
            throw new DataNotFoundException("User with id = " + ownerId + " is not an owner!");
        }

        switch (approve.toLowerCase()) {
            case "true": {
                if (bookingDto.getStatus().equals(BookingStatus.APPROVED)) {
                    throw new DataValidationException("Status is Approved");
                }
                bookingDto.setStatus(BookingStatus.APPROVED);
                break;
            }
            case "false": {
                bookingDto.setStatus(BookingStatus.REJECTED);
                break;
            }
            default:
                throw new DataValidationException("Incorrect data in approve method");
        }
        Booking bookingToUpdate = BookingMapper.toBookingUpdate(bookingDto,
                bookingRepository.findById(bookingId).get());
        bookingRepository.save(bookingToUpdate);
        return BookingMapper.toBookingDto(bookingToUpdate);
    }

    @Override
    public BookingDto getBookingInfo(Long bookingId, Long userId) {
        checkingUserId(userId);
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("There is no Booking with Id: " + bookingId)));
        if (!Objects.equals(bookingDto.getItem().getOwner().getId(), userId)
                && !Objects.equals(bookingDto.getBooker().getId(), userId)) {
            throw new DataNotFoundException("User with id = " + userId + " is not an owner!");
        }
        return bookingDto;
    }

    @Override
    public List<BookingDto> getAllBookingsByUserId(Long userId, String state) {
        checkingUserId(userId);
        checkingBookingState(state);
        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "WAITING": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndWaitingStatus(userId,
                        BookingStatus.WAITING, SORT_BY_START_DESC));
                break;
            }
            case "REJECTED": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndRejectedStatus(userId,
                        List.of(BookingStatus.REJECTED, BookingStatus.CANCELED), SORT_BY_START_DESC));
                break;
            }
            case "CURRENT": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndCurrentStatus(userId,
                        LocalDateTime.now(), SORT_BY_START_DESC));
                break;
            }
            case "FUTURE": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndFutureStatus(userId,
                        LocalDateTime.now(), SORT_BY_START_DESC));
                break;
            }
            case "PAST": {
                bookings = new ArrayList<>(bookingRepository.findAllByBookerIdAndPastStatus(userId,
                        LocalDateTime.now(), SORT_BY_START_DESC));
                break;
            }
            case "ALL": {
                bookings = new ArrayList<>(bookingRepository.findAllByBooker_Id(userId, SORT_BY_START_DESC));
                break;
            }
            default:
                bookings = new ArrayList<>();
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingsByOwnerId(Long ownerId, String state) {
        checkingUserId(ownerId);
        checkingBookingState(state);
        List<Long> userItemsIds = itemRepository.findByOwner_Id(ownerId, Sort.by(Sort.Direction.ASC, "id"))
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
                        BookingStatus.WAITING, SORT_BY_START_DESC));
                break;
            }
            case "REJECTED": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndRejectedStatus(userItemsIds,
                        List.of(BookingStatus.REJECTED, BookingStatus.CANCELED), SORT_BY_START_DESC));
                break;
            }
            case "CURRENT": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndCurrentStatus(userItemsIds,
                        LocalDateTime.now(), SORT_BY_START_DESC));
                break;
            }
            case "FUTURE": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndFutureStatus(userItemsIds,
                        LocalDateTime.now(), SORT_BY_START_DESC));
                break;
            }
            case "PAST": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItemsAndPastStatus(userItemsIds,
                        LocalDateTime.now(), SORT_BY_START_DESC));
                break;
            }
            case "ALL": {
                bookings = new ArrayList<>(bookingRepository.findAllByOwnerItems(userItemsIds, SORT_BY_START_DESC));
                break;
            }
            default:
                bookings = new ArrayList<>();
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private UserDto checkingUserId(Long userId) {
        if (userId == -1) {
            throw new DataNotFoundException("Пользователя под номером : " + userId + " не существует!");
        }
        return UserMapper.toUserDto(userRepository.findById(userId).orElseThrow(() ->
                new DataNotFoundException("There is no user with id: " + userId)));
    }

    private void checkingBookingState(String state) {
        try {
            State.valueOf(state);
        } catch (Exception e) {
            throw new UnsupportedStatusException("Unknown state: " + state);
        }
    }
}

package ru.practicum.shareit.booking.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.validator.PageValidatorService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RequestMapping(path = "/bookings")
@RestController
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final BookingService service;
    private final PageValidatorService validator;

    @PostMapping
    public BookingDto createBooking(@RequestBody BookingDto bookingDto,
                             @RequestHeader(USER_ID) Long bookerId) {
        log.info("Получен POST-запрос к эндпоинту: '/bookings' " +
                "на создание бронирования от пользователя с ID={}", bookerId);
        return service.addBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable Long bookingId,
                             @RequestHeader(USER_ID) Long userId, @RequestParam String approved) {
        log.info("Получен PATCH-запрос к эндпоинту: '/bookings' на обновление статуса бронирования с ID={}", bookingId);
        return service.approveBooking(bookingId, userId, approved);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsForUser(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                  @RequestHeader(USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        validator.checkingPageableParams(from, size);
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение " +
                "списка всех бронирований пользователя с ID={} с параметром STATE={}", userId, state);
        Pageable page = PageRequest.of(from / size, size);
        return service.getAllBookingsByUserId(userId, state, page);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsForOwner(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @RequestHeader(USER_ID) Long userId,
                                                   @RequestParam(defaultValue = "0") Integer from,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        validator.checkingPageableParams(from, size);
        log.info("Получен GET-запрос к эндпоинту: '/bookings/owner' на получение " +
                "списка всех бронирований вещей пользователя с ID={} с параметром STATE={}", userId, state);
        Pageable page = PageRequest.of(from / size, size);
        return service.getAllBookingsByOwnerId(userId, state, page);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getInfoForBooking(@PathVariable Long bookingId,
                                        @RequestHeader(USER_ID) Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение бронирования с ID={}", bookingId);
        return service.getBookingInfo(bookingId, userId);
    }
}

package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.validator.BookingValidator;
import ru.practicum.shareit.validator.PageValidator;


/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RequestMapping(path = "/bookings")
@RestController
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final BookingClient bookingClient;
    private final PageValidator validator;
    private final BookingValidator bookingValidator;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestBody BookingDto bookingDto,
                                                @RequestHeader(USER_ID) Long bookerId) {
        bookingValidator.validateBookingData(bookingDto);
        log.info("Получен POST-запрос к эндпоинту: '/bookings' " +
                "на создание бронирования от пользователя с ID={}", bookerId);
        return bookingClient.createBooking(bookerId,bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@PathVariable Long bookingId,
                             @RequestHeader(USER_ID) Long userId, @RequestParam String approved) {
        if (!approved.equals("true") && !approved.equals("false")) {
            throw new DataValidationException("Невозможное подтверждение!");
        }
        log.info("Получен PATCH-запрос к эндпоинту: '/bookings' на обновление статуса бронирования с ID={}", bookingId);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsForUser(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                  @RequestHeader(USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        validator.checkingPageableParams(from, size);
        bookingValidator.validateBookingState(state);
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение " +
                "списка всех бронирований пользователя с ID={} с параметром STATE={}", userId, state);
        return bookingClient.getAllBookingsByUserId(userId, from, size, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsForOwner(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @RequestHeader(USER_ID) Long userId,
                                                   @RequestParam(defaultValue = "0") Integer from,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        validator.checkingPageableParams(from, size);
        bookingValidator.validateBookingState(state);
        log.info("Получен GET-запрос к эндпоинту: '/bookings/owner' на получение " +
                "списка всех бронирований вещей пользователя с ID={} с параметром STATE={}", userId, state);
        return bookingClient.getAllBookingsForOwner(userId, from, size, state);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getInfoForBooking(@PathVariable Long bookingId,
                                        @RequestHeader(USER_ID) Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/bookings' на получение бронирования с ID={}", bookingId);
        return bookingClient.getBookingInfo(userId, bookingId);
    }
}

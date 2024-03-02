package ru.practicum.shareit.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;

@Component
@RequiredArgsConstructor
public class BookingValidator {
    private final BookingRepository bookingRepository;

    public BookingDto validateAndReturnsBookingDto(long bookingId) {
        if (bookingId < 0) {
            throw new DataNotFoundException("Номер бронирования не может быть меньше 0");
        }
        return BookingMapper.toBookingDto(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Бронирования под номером: " + bookingId +
                        " не существует!")));
    }

    public Booking validateAndReturnsBooking(long bookingId) {
        if (bookingId < 0) {
            throw new DataNotFoundException("Номер бронирования не может быть меньше 0");
        }
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Бронирования под номером: " + bookingId +
                        " не существует!"));
    }

    public void checkingBookingState(String state) {
        try {
            State.valueOf(state);
        } catch (Exception e) {
            throw new UnsupportedStatusException("Unknown state: " + state);
        }
    }
}

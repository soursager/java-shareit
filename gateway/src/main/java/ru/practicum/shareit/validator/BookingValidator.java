package ru.practicum.shareit.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BookingValidator {
    public void validateBookingState(String state) {
        String result = checkingBookingState(state);
        if (result.isEmpty()) {
            throw new UnsupportedStatusException(state);
        }
    }

    public void validateBookingData(BookingDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new DataValidationException("Booking: Дата не может быть null!");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) || bookingDto.getStart().isEqual(bookingDto.getEnd())
                || bookingDto.getEnd().isBefore(LocalDateTime.now()) || bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new DataValidationException("Booking: введены неверные даты");
        }
    }

    public String checkingBookingState(String state) {
        try {
            State.valueOf(state);
        } catch (Exception e) {
            throw new UnsupportedStatusException("Unknown state: " + state);
        }
        return state;
    }
}
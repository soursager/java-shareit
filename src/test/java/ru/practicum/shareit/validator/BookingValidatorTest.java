package ru.practicum.shareit.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingValidatorTest {

    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    BookingValidator bookingValidator;

    @Test
    void validateBookingState_whenStateIsIncorrect_thenThrowUnsupportedStatusException() {
        UnsupportedStatusException exception = assertThrows(UnsupportedStatusException.class,
                () -> bookingValidator.checkingBookingState("NOT"));

        assertEquals(exception.getMessage(), "Unknown state: NOT" );
    }

    @Test
    void validateBookingId_whenBookingNotExists_thenThrowEntityNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenThrow(new DataNotFoundException("Бронирования не существует"));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingValidator.validateAndReturnsBooking(1L));

        assertEquals(exception.getMessage(), "Бронирования не существует");
    }

    @Test
    void validateBookingId_whenBookingIdLessThanZero_thenThrowIncorrectDataException() {
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingValidator.validateAndReturnsBooking(-1L));

        assertEquals(exception.getMessage(), "Номер бронирования не может быть меньше 0");
    }

    @Test
    void validateBookingIdAndReturns_whenBookingNotFound_thenThrowEntityNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenThrow(new DataNotFoundException("Бронирования не существует"));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> bookingValidator.validateAndReturnsBookingDto(1L));

        assertEquals(exception.getMessage(), "Бронирования не существует");
    }

    @Test
    void validateBookingIdAndReturns_whenBookingExists_thenReturnBooking() {
        Booking booking = new Booking();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Booking actual =  bookingValidator.validateAndReturnsBooking(1L);

        assertEquals(actual, booking);
    }
}

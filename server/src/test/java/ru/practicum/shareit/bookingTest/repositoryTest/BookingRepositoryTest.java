package ru.practicum.shareit.bookingTest.repositoryTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class BookingRepositoryTest {
    private static final Pageable PAGE_FOR_BOOKINGS = PageRequest.of(0, 10,
            Sort.by("start").descending());
   private long bookerId;
   private long ownerId;
   private long itemId;

    @Autowired
   private UserRepository userRepository;

    @Autowired
   private BookingRepository bookingRepository;

    @Autowired
   private ItemRepository itemRepository;

    @BeforeEach
    public void addRequests() {
        User booker = User.builder()
                .email("mail1@mail.ru")
                .name("name1")
                .build();
        userRepository.save(booker);
        bookerId = booker.getId();

        User owner = User.builder()
                .email("mail2@mail.ru")
                .name("name2")
                .build();
        userRepository.save(owner);
        ownerId = owner.getId();

        Item item = itemRepository.save(Item.builder()
                .name("item1")
                .description("desc")
                .owner(owner)
                .available(true)
                .build());
        itemId = item.getId();

        //booker_pastStatus
        bookingRepository.save(Booking.builder()
                .booker(booker)
                .end(LocalDateTime.now().minusDays(1))
                .start(LocalDateTime.now().minusDays(2))
                .build());
        //booker_futureStatus
        bookingRepository.save(Booking.builder()
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());
        //booker_currentStatus
        bookingRepository.save(Booking.builder()
                .booker(booker)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());
        //booker_statusRejected_andPastStatus
        bookingRepository.save(Booking.builder()
                .booker(booker)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .status(BookingStatus.REJECTED)
                .build());
        //booker_statusWaiting_andPastStatus_andOwnerBooking_PastOwnerStatus
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .status(BookingStatus.WAITING)
                .build());


        //owner_pastStatus
        bookingRepository.save(Booking.builder()
                .item(item)
                .end(LocalDateTime.now().minusDays(1))
                .start(LocalDateTime.now().minusDays(2))
                .build());
        //owner_futureStatus
        bookingRepository.save(Booking.builder()
                .item(item)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());
        //owner_currentStatus
        bookingRepository.save(Booking.builder()
                .item(item)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());
        //owner_statusRejected_andPastStatus
        bookingRepository.save(Booking.builder()
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .status(BookingStatus.REJECTED)
                .build());
        //owner_statusWaiting_andPastStatus
        bookingRepository.save(Booking.builder()
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .status(BookingStatus.WAITING)
                .build());
    }

    @Test
    void findAllByBookerId_whenBookerBookingsEqualsFive_thenReturnListOfFiveBookings() {
        List<Booking> bookerBookings = bookingRepository.findAllByBooker_Id(bookerId, PAGE_FOR_BOOKINGS);

        assertEquals(bookerBookings.size(), 5);
        assertEquals(bookerBookings.get(0).getBooker().getName(),"name1");
        assertEquals(bookerBookings.get(2).getBooker().getEmail(), "mail1@mail.ru");
    }

    @Test
    void findAllByItemOwnerId_whenOwnerHasSixBookings_returnListOfSixBookings() {
        List<Booking> ownerBookings = bookingRepository.findAllByItem_Owner_Id(ownerId);

        assertEquals(ownerBookings.size(), 6);
        assertEquals(ownerBookings.get(0).getItem().getName(),"item1");
        assertEquals(ownerBookings.get(2).getItem().getDescription(), "desc");
    }

    @Test
    void findAllByUserIdAndItemIdAndEndDateIsPassed_whenBookerHasOneItemWithEndDatePassed_thenReturnListOfOneBooking() {
        List<Booking> ownerBookings = bookingRepository.findAllByUserIdAndItemIdAndEndDateIsPassed(bookerId,
                itemId, LocalDateTime.now());

        assertEquals(ownerBookings.size(), 1);
        assertEquals(ownerBookings.get(0).getId(),5);
        assertEquals(ownerBookings.get(0).getBooker().getName(), "name1");
        assertEquals(ownerBookings.get(0).getBooker().getEmail(), "mail1@mail.ru");
    }

    @Test
    void findAllByOwnerItems_whenOwnerHasSixBookings_returnListOfSixBookings() {
        List<Booking> itemsBookings = bookingRepository.findAllByOwnerItems(List.of(itemId), PAGE_FOR_BOOKINGS);

        assertEquals(itemsBookings.size(), 6);
        assertEquals(itemsBookings.get(0).getItem().getName(), "item1");
        assertEquals(itemsBookings.get(2).getItem().getDescription(), "desc");
    }

    @Test
    void findAllByOwnerItemsAndWaitingStatus_whenOwnerHasTwoBookingWithWaitingStatus_thenReturnListOfTwoBookings() {
        List<Booking> ownerBookings = bookingRepository.findAllByOwnerItemsAndWaitingStatus(List.of(itemId),
                BookingStatus.WAITING, PAGE_FOR_BOOKINGS);

        assertEquals(ownerBookings.size(), 2);
        assertEquals(ownerBookings.get(0).getItem().getName(),"item1");
        assertEquals(ownerBookings.get(1).getItem().getDescription(),"desc");
    }

    @Test
    void findAllByOwnerItemsAndRejectedStatus_whenOwnerHasOneBookingWithRejectedStatus_thenReturnListOfOneBooking() {
        List<Booking> ownerBookings = bookingRepository.findAllByOwnerItemsAndRejectedStatus(List.of(itemId),
                List.of(BookingStatus.REJECTED, BookingStatus.CANCELED), PAGE_FOR_BOOKINGS);

        assertEquals(ownerBookings.size(), 1);
        assertEquals(ownerBookings.get(0).getItem().getName(), "item1");
        assertEquals(ownerBookings.get(0).getItem().getDescription(), "desc");
        assertEquals(ownerBookings.get(0).getStatus().toString(), "REJECTED");
    }

    @Test
    void findAllByOwnerItemsAndCurrentStatus_whenOwnerHasOneBookingWithCurrentStatus_thenReturnListOfOneBooking() {
        List<Booking> ownerBookings = bookingRepository.findAllByOwnerItemsAndCurrentStatus(List.of(itemId),
                LocalDateTime.now(), PAGE_FOR_BOOKINGS);

        assertEquals(ownerBookings.size(), 1);
        assertEquals(ownerBookings.get(0).getItem().getName(), "item1");
        assertEquals(ownerBookings.get(0).getItem().getDescription(), "desc");
    }

    @Test
    void findAllByOwnerItemsAndFutureStatus_whenOwnerHasOneBookingWithFutureStatus_thenReturnListOfOneBooking() {
        List<Booking> ownerBookings = bookingRepository.findAllByOwnerItemsAndFutureStatus(List.of(itemId),
                LocalDateTime.now(), PAGE_FOR_BOOKINGS);

        assertEquals(ownerBookings.size(), 1);
        assertEquals(ownerBookings.get(0).getItem().getName(), "item1");
        assertEquals(ownerBookings.get(0).getItem().getDescription(), "desc");
    }

    @Test
    void findAllByOwnerItemsAndPastStatus_whenOwnerHasFourBookingWithPastStatus_thenReturnListOfFourBookings() {
        List<Booking> ownerBookings = bookingRepository.findAllByOwnerItemsAndPastStatus(List.of(itemId),
                LocalDateTime.now(), PAGE_FOR_BOOKINGS);

        assertEquals(ownerBookings.size(), 4);
        assertEquals(ownerBookings.get(1).getItem().getName(), "item1");
        assertEquals(ownerBookings.get(2).getItem().getDescription(), "desc");
    }

    @Test
    void findAllByBookerIdAndWaitingStatus_whenThereIsOneWaitingStatusBooking_thenReturnListOfOneBooking() {
        List<Booking> pastBookerBookings = bookingRepository.findAllByBookerIdAndWaitingStatus(bookerId,
                BookingStatus.WAITING, PAGE_FOR_BOOKINGS);

        assertEquals(pastBookerBookings.size(), 1);
        assertEquals(pastBookerBookings.get(0).getBooker().getName(), "name1");
        assertEquals(pastBookerBookings.get(0).getBooker().getEmail(), "mail1@mail.ru");
    }

    @Test
    void findAllByBookerIdAndRejectedStatus_whenThereIsOneRejectedStatusBooking_thenReturnListOfOneBooking() {
        List<Booking> pastBookerBookings = bookingRepository.findAllByBookerIdAndRejectedStatus(bookerId,
                List.of(BookingStatus.REJECTED, BookingStatus.CANCELED), PAGE_FOR_BOOKINGS);

        assertEquals(pastBookerBookings.size(), 1);
        assertEquals(pastBookerBookings.get(0).getBooker().getName(), "name1");
        assertEquals(pastBookerBookings.get(0).getBooker().getEmail(), "mail1@mail.ru");
    }

    @Test
    void findAllByBookerIdAndCurrentStatus_whenThereIsOneCurrentStatusBooking_thenReturnListOfOneBooking() {
        List<Booking> pastBookerBookings = bookingRepository.findAllByBookerIdAndCurrentStatus(bookerId,
                LocalDateTime.now(), PAGE_FOR_BOOKINGS);

        assertEquals(pastBookerBookings.size(), 1);
        assertEquals(pastBookerBookings.get(0).getBooker().getName(), "name1");
        assertEquals(pastBookerBookings.get(0).getBooker().getEmail(), "mail1@mail.ru");
    }

    @Test
    void findAllByBookerIdAndFutureStatus_whenThereIsOneFutureStatusBooking_thenReturnListOfOneBooking() {
        List<Booking> pastBookerBookings = bookingRepository.findAllByBookerIdAndFutureStatus(bookerId,
                LocalDateTime.now(), PAGE_FOR_BOOKINGS);

        assertEquals(pastBookerBookings.size(), 1);
        assertEquals(pastBookerBookings.get(0).getBooker().getName(), "name1");
        assertEquals(pastBookerBookings.get(0).getBooker().getEmail(), "mail1@mail.ru");
    }

    @Test
    void findAllByBookerIdAndPastStatus_whenThereAreThreePastStatusBookings_thenReturnListOfThreeBookings() {
        List<Booking> pastBookerBookings = bookingRepository.findAllByBookerIdAndPastStatus(bookerId,
                LocalDateTime.now(), PAGE_FOR_BOOKINGS);

        assertEquals(pastBookerBookings.size(), 3);
        assertEquals(pastBookerBookings.get(1).getBooker().getName(), "name1");
        assertEquals(pastBookerBookings.get(2).getBooker().getEmail(), "mail1@mail.ru");
    }

    @AfterEach
    public void deleteItems() {
        bookingRepository.deleteAll();
    }
}

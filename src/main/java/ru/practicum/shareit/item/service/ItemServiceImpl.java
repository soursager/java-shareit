package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto createItemDto(ItemDto itemDto, Long userId) {
        Item newItem = ItemMapper.toItem(itemDto);
        newItem.setOwner(returnUserIfExists(userId));
        return ItemMapper.toItemDto(itemRepository.save(newItem));
    }

    @Transactional
    @Override
    public ItemDto updateItemDto(ItemDto itemDto, long userId, long itemId) {
         checkingUserId(userId);
         Item itemOld = returnItemIfExists(itemId);
        if (!itemOld.getOwner().getId().equals(userId)) {
            throw new DataNotFoundException("У пользователя нет такой вещи!");
        }
        if (itemDto.getName() != null) {
            itemOld.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            itemOld.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            itemOld.setAvailable(itemDto.getAvailable());
        }

        itemRepository.save(itemOld);
        return ItemMapper.toItemDto(itemOld);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getItemDtoById(long itemId, long userId) {
        checkingUserId(userId);
        List<CommentDto> commentsForItem = commentRepository.findAllByItem_Id(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        List<BookingDto> bookingsForItem = getOwnerBooking(userId)
                .stream()
                .filter(x -> x.getItem().getId().equals(itemId))
                .collect(Collectors.toList());

        if (!bookingsForItem.isEmpty() && !commentsForItem.isEmpty()) {
            return ItemMapper.toItemDtoWithBookingsAndComments(itemRepository.findById(itemId)
                    .orElseThrow(() -> new DataNotFoundException("Несуществующий предмет под номером " + itemId)),
                    bookingsForItem, commentsForItem);
        } else if (!bookingsForItem.isEmpty()) {
            return ItemMapper.toItemDtoWithBookingsAndComments(itemRepository.findById(itemId)
                            .orElseThrow(() -> new DataNotFoundException("Несуществующий предмет под номером " + itemId)),
                    bookingsForItem, commentsForItem);
        } else {
            return ItemMapper.toItemDtoWithComments(itemRepository.findById(itemId)
                            .orElseThrow(() -> new DataNotFoundException("Несуществующий предмет под номером " + itemId)),
                    commentsForItem);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> getItemsDtoByUserId(long userId) {
        UserDto userFromDb = checkingUserId(userId);

        List<Item> userItems = new ArrayList<>(itemRepository.findByOwner_Id(userFromDb.getId(),
                Sort.by(Sort.Direction.ASC, "id")));
        List<CommentDto> commentsToUserItems = commentRepository.findAllByItemsUser_Id(userId,
                        Sort.by(Sort.Direction.DESC, "created"))
                .stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
        List<BookingDto> bookingsToUserItems = getOwnerBooking(userId);

        Map<Item, List<BookingDto>> itemsWithBookingsMap = new HashMap<>();
        Map<Item, List<CommentDto>> itemsWithCommentsMap = new HashMap<>();

        for (Item i : userItems) {
            itemsWithCommentsMap.put(i, commentsToUserItems.stream()
                    .filter(c -> c.getItem().getId().equals(i.getId()))
                    .collect(Collectors.toList()));
            itemsWithBookingsMap.put(i, bookingsToUserItems.stream()
                    .filter(b -> b.getItem().getId().equals(i.getId()))
                    .collect(Collectors.toList()));
        }
        List<ItemDto> results = new ArrayList<>();
        for (Item i : userItems) {
            results.add(ItemMapper.toItemDtoWithBookingsAndComments(i, itemsWithBookingsMap.get(i),
                    itemsWithCommentsMap.get(i)));
        }
        return results;
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> getItemsDtoBySearch(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.getItemsBySearchQuery(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addCommentToItem(Long userId, Long itemId, CommentDto commentDto) {
        if (commentDto.getText().isEmpty()) {
            throw new DataValidationException("Комментарий не может быть пустым");
        }
        UserDto author = checkingUserId(userId);
        List<BookingDto> bookings = bookingRepository.findAllByUserIdAndItemIdAndEndDateIsPassed(userId, itemId,
                        LocalDateTime.now())
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
        if (bookings.isEmpty()) {
            throw new DataValidationException("Данный пользователь не бронировал вещь");
        }
        ItemDto item = getItemDtoById(itemId, userId);
        commentDto = CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto,
                UserMapper.toUser(author), ItemMapper.toItem(item))));
        return commentDto;
    }

    private User returnUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь по id - " + userId + " не найден"));
    }

    public Item returnItemIfExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Вещь по id - " + itemId + " не найдена"));
    }

    private UserDto checkingUserId(long userId) {
        if (userId == -1) {
            throw new DataNotFoundException("Необходим номер пользователя");
        }
        return UserMapper.toUserDto(userRepository.findById(userId).orElseThrow(() ->
                new DataNotFoundException("Польователя под номером " + userId + " не существует!")));
    }

    private List<BookingDto> getOwnerBooking(Long ownerId) {
        return bookingRepository.findAllByItem_Owner_Id(ownerId)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

}

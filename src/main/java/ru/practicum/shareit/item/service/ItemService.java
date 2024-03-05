package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItemDto(ItemDto item, Long userId);

    ItemDto updateItemDto(ItemDto itemDto, long userId, long itemId);

    ItemDto getItemDtoById(long itemId, long userId);

    Collection<ItemDto> getItemsDtoByUserId(long userId, Pageable page);

    Collection<ItemDto> getItemsDtoBySearch(String text, Pageable page);

    CommentDto addCommentToItem(Long userId, Long itemId, CommentDto commentDto);
}

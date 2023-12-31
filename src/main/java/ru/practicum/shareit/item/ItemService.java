package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItemDto(ItemDto item, Long userId);

    ItemDto updateItemDto(ItemDto itemDto, long userId, long itemId);

    ItemDto getItemDtoById(long itemId, long userId);

    Collection<ItemDto> getItemsDtoByUserId(long userId);

    Collection<ItemDto> getItemsDtoBySearch(String text);
}

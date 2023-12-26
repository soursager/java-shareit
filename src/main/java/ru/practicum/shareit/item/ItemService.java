package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper mapper;

     public ItemDto createItemDto(ItemDto itemDto, Long userId) {
        return mapper.toItemDto(itemStorage.create(mapper.toItem(itemDto), userId));
    }

    public ItemDto updateItemDto(ItemDto itemDto, long userId, long itemId) {
         itemDto.setId(itemId);
         return mapper.toItemDto(itemStorage.update(mapper.toItem(itemDto), userId));
    }

    public ItemDto getItemDtoById(long itemId, long userId) {
        return mapper.toItemDto(itemStorage.getItemById(itemId, userId));
    }

    public Collection<ItemDto> getItemsDtoByUserId(long userId) {
        return itemStorage.getItemsByUserId(userId).stream()
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }

    public Collection<ItemDto> getItemsDtoBySearch(String text) {
        text = text.toLowerCase();
        return itemStorage.getItemsBySearch(text).stream()
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }
}

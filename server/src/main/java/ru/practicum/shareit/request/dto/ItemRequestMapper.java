package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requester) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .creationDate(itemRequestDto.getCreated())
                .requester(requester)
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        List<ItemForRequestDto> items = null;
        if (itemRequest.getResponsesToRequest() != null) {
            items = itemRequest.getResponsesToRequest().stream()
                    .map(ItemRequestMapper::makeResultItemDto)
                    .collect(Collectors.toList());
        }

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreationDate())
                .items(items)
                .build();

        if (itemRequest.getRequester() != null) {
            UserDto requester = toUserDto(itemRequest.getRequester());
            requestDto.setRequester(requester);
        }

        return requestDto;
    }

    public static ItemForRequestDto makeResultItemDto(Item item) {
        return ItemForRequestDto.builder()
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .id(item.getId())
                .requestId(item.getRequest().getId())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }
}

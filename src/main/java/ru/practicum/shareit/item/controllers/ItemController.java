package ru.practicum.shareit.item.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemServiceImpl itemService;
    private static final String OWNER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader(OWNER) Long ownerId) {
        log.info("Выполняется запрос на добавление вещи у пользователя {}", ownerId);
        return itemService.createItemDto(itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId, @RequestHeader(OWNER) Long ownerId) {
        log.info("Выполняется запрос на получение вещи под номером {}", itemId);
        return itemService.getItemDtoById(itemId, ownerId);
    }

    @GetMapping
    public Collection<ItemDto> getItemsByOwner(@RequestHeader(OWNER) Long ownerId) {
        log.info("Выполняется запрос на вывод всех вещей пользователя под номером {}", ownerId);
        return itemService.getItemsDtoByUserId(ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                          @RequestHeader(OWNER) Long ownerId) {
        log.info("Выполняется запрос на обновление вещи под номером {}", ownerId);
        return itemService.updateItemDto(itemDto, ownerId, itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> getItemsBySearchQuery(@RequestParam String text) {
        log.info("Выполняется запрос поиска вещи по строке {}", text);
        return itemService.getItemsDtoBySearch(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createCommentToItem(@PathVariable Long itemId, @RequestBody CommentDto comment,
                                          @RequestHeader(OWNER) Long userId) {
        log.debug("Создание комментария к элементу по идентификатору пользователя {}", userId);
        comment.setCreated(LocalDateTime.now());
        return itemService.addCommentToItem((long) userId, itemId, comment);
    }
}

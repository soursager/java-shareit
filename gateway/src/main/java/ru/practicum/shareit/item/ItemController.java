package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validator.ItemValidator;
import ru.practicum.shareit.validator.PageValidator;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;
    private final PageValidator validator;
    private final ItemValidator itemValidator;
    private static final String OWNER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemDto itemDto, @RequestHeader(OWNER) Long ownerId) {
        itemValidator.validateItemData(itemDto);
        log.info("Выполняется запрос на добавление вещи у пользователя {}", ownerId);
        return itemClient.createItemDto(ownerId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId, @RequestHeader(OWNER) Long ownerId) {
        log.info("Выполняется запрос на получение вещи под номером {}", itemId);
        return itemClient.getItemDtoById(ownerId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader(OWNER) Long ownerId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        validator.checkingPageableParams(from, size);
        log.info("Выполняется запрос на вывод всех вещей пользователя под номером {}", ownerId);
        return itemClient.getItemsDtoByUserId(ownerId, from, size);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                          @RequestHeader(OWNER) Long ownerId) {
        itemValidator.validateItemDataUpdate(itemDto);
        log.info("Выполняется запрос на обновление вещи под номером {}", ownerId);
        return itemClient.updateItemDto(ownerId, itemId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsBySearchQuery(@RequestParam String text,
                                                     @RequestParam(defaultValue = "0") Integer from,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     @RequestHeader(OWNER) Long ownerId) {
        validator.checkingPageableParams(from, size);
        log.info("Выполняется запрос поиска вещи по строке {}", text);
        return itemClient.getItemsDtoBySearch(ownerId, from, size, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createCommentToItem(@PathVariable Long itemId, @RequestBody CommentDto comment,
                                          @RequestHeader(OWNER) Long userId) {
        log.debug("Создание комментария к элементу по идентификатору пользователя {}", userId);
        comment.setCreated(LocalDateTime.now());
        return itemClient.addCommentToItem(userId, itemId, comment);
    }
}

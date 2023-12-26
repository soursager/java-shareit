package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String OWNER = "X-Sharer-User-Id";

    @ResponseBody
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

    @ResponseBody
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
}

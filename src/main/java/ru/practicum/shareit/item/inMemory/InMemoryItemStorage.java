package ru.practicum.shareit.item.inMemory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long itemId = 0L;

    @Override
    public Item create(Item item) {
        if (isValidItem(item)) {
            item.setId(++itemId);
            items.put(item.getId(), item);
        }
        return item;
    }

    @Override
    public Item update(Item item, Long userId) {
        if (item.getId() == null) {
            throw new DataValidationException("Не передан параметр!");
        }
        if (!items.containsKey(item.getId())) {
            throw new DataNotFoundException("Вещь под номером " + item.getId() + " не найдена!");
        }
        Item itemOld = items.get(item.getId());
        if (!itemOld.getOwner().getId().equals(userId)) {
            throw new DataNotFoundException("У пользователя нет такой вещи!");
        }
        if (item.getName() == null) {
            item.setName(itemOld.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(itemOld.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(itemOld.getAvailable());
        }
        item.setOwner(itemOld.getOwner());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getItemById(long itemId) {
        if (!items.containsKey(itemId)) {
            throw new DataNotFoundException("Вещь под номером " + itemId + " не найдена!");
        }
        return items.get(itemId);
    }

    @Override
    public Collection<Item> getItemsByUserId(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> getItemsBySearch(String text) {
        Collection<Item> itemsList = new ArrayList<>();
        if (text.isBlank()) {
            return itemsList;
        }
            itemsList.addAll(items.values().stream()
                    .filter(item -> item.getAvailable().equals(true))
                    .filter(item -> item.getName().toLowerCase().contains(text) ||
                            item.getDescription().toLowerCase().contains(text))
                    .collect(Collectors.toList()));
        return itemsList;
    }

    private boolean isValidItem(Item item) {
        if ((item.getName().isEmpty()) || (item.getDescription().isEmpty()) || (item.getAvailable() == null)) {
            throw new DataValidationException("У вещи некорректные данные");
        }
        return true;
    }
}

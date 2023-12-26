package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InMemoryItemStorage implements ItemStorage {
    private final UserStorage userStorage;
    private final Map<Long, List<Item>> items = new HashMap<>();
    private Long itemId = 0L;

    @Override
    public Item create(Item item, Long userId) {
        item.setOwner(userStorage.getUserById(userId));
        if (isValidItem(item)) {
            item.setId(++itemId);
        }
        if (items.containsKey(userId)) {
            items.get(userId).add(item);
        } else {
            ArrayList<Item> itemsForUser = new ArrayList<>();
            itemsForUser.add(item);
            items.put(userId, itemsForUser);
        }
        return item;
    }

    @Override
    public Item update(Item item, long userId) {
        if (item.getId() == null) {
            throw new DataValidationException("Не передан параметр!");
        }
        int index = getIndexForItem(item.getId(), userId);
        Item itemOld = items.get(userId).get(index);
        if (item.getName() == null) {
            item.setName(itemOld.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(itemOld.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(itemOld.getAvailable());
        }
        items.get(userId).set(index, item);
        return item;
    }

    @Override
    public Item getItemById(long itemId, long userId) {
        Item itemFromId = null;
        for (long userIdForSearch : items.keySet()) {
            itemFromId = items.get(userIdForSearch).stream()
                    .filter(x -> x.getId() == itemId)
                    .findFirst()
                    .orElse(null);
        }
        if (itemFromId == null) {
            throw new DataNotFoundException("Данной вещи не существует!");
        }
        return itemFromId;
    }

    @Override
    public Collection<Item> getItemsByUserId(long userId) {
        return items.get(userId);
    }

    @Override
    public Collection<Item> getItemsBySearch(String text) {
        Collection<Item> itemsList = new ArrayList<>();
        if (text.isBlank()) {
            return itemsList;
        }
        for (long userId : items.keySet()) {
            itemsList.addAll(items.get(userId).stream()
                    .filter(item -> item.getAvailable().equals(true))
                    .filter(item -> item.getName().toLowerCase().contains(text) ||
                            item.getDescription().toLowerCase().contains(text))
                    .collect(Collectors.toList()));
        }
        return itemsList;
    }

    private int getIndexForItem(long itemId, long userId) {
        if (items.get(userId) == null) {
            throw new DataNotFoundException("У данного пользователя пуст список вещей!");
        }
        Item itemOld = items.get(userId).stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);
        if (itemOld == null) {
            throw new DataNotFoundException("Данный пользователь не является хозяином вещи!");
        }
        return items.get(userId).indexOf(itemOld);
    }

    private boolean isValidItem(Item item) {
        if ((item.getName().isEmpty()) || (item.getDescription().isEmpty()) || (item.getAvailable() == null)) {
            throw new DataValidationException("У вещи некорректные данные");
        }
        return true;
    }
}

package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {

    Item create(Item item, Long userId);

    Item update(Item item, long userId);

    Item getItemById(long itemId, long userId);

    Collection<Item> getItemsByUserId(long userId);

    Collection<Item> getItemsBySearch(String text);
}

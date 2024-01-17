package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {

    Item create(Item item);

    Item update(Item item, Long userId);

    Item getItemById(long itemId);

    Collection<Item> getItemsByUserId(long userId);

    Collection<Item> getItemsBySearch(String text);
}

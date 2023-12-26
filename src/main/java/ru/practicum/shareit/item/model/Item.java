package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class Item {
    private Long id;                 // уникальный идентификатор вещи
    @NotBlank
    private String name;             // краткое название
    private String description;      // развёрнутое описание
    private Boolean available;       // статус о том, доступна или нет вещь для аренды
    private User owner;              // владелец вещи
    private ItemRequest request;     // если вещь была создана по запросу другого пользователя, то в этом
                                     // поле хранится ссылка на соответствующий запрос
}

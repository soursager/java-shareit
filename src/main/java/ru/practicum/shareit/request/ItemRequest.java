package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequest {
    private Long id;                      // идентификатор запроса
    private String description;           // текст запроса, содержащий описание требуемой вещи
    private User requester;               // пользователь, создавший запрос
    private LocalDateTime createdTime;    // дата и время создания запроса
}

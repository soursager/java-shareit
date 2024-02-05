package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                 // уникальный идентификатор вещи

    @NotBlank
    @Column(name = "name")
    private String name;             // краткое название

    @Column(name = "description")
    private String description;      // развёрнутое описание

    @Column(name = "available")
    private Boolean available;       // статус о том, доступна или нет вещь для аренды

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;              // владелец вещи

    private Long requestId;     // если вещь была создана по запросу другого пользователя, то в этом
                                     // поле хранится ссылка на соответствующий запрос
}

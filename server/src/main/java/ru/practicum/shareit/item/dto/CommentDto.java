package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;                // уникальный идентификатор комментария;
    @NotEmpty
    @NotBlank
    private String text;            // содержимое комментария;
    @JsonIgnore
    private ItemDto item;              // вещь, к которой относится комментарий;
    private String authorName;          // имя автора комментария;
    private LocalDateTime created;  // дата создания комментария.
}

package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemForRequestDto {
    private Long id;
    @NotBlank
    private String name;
    private Long ownerId;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;
    private Long requestId;
}

package ru.practicum.shareit.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.ItemDto;

@Component
@RequiredArgsConstructor
public class ItemValidator {
    public void validateItemData(ItemDto itemDto) {
        if (itemDto.getAvailable() == null || itemDto.getDescription() == null || itemDto.getName() == null) {
            throw new DataValidationException("Невозможно использовать пустые поля!");
        }
        if (itemDto.getName().isEmpty() || itemDto.getDescription().isEmpty()) {
            throw new DataValidationException("Невозможно использовать пустые поля!");
        }
    }

    public void validateItemDataUpdate(ItemDto itemDto) {
        if (itemDto.getAvailable() == null && itemDto.getDescription() == null && itemDto.getName() == null) {
            throw new DataValidationException("Невозможно использовать пустые поля!");
        }
    }
}

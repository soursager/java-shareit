package ru.practicum.shareit.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

@Component
@RequiredArgsConstructor
public class ItemValidatorService {
    private final ItemRepository itemRepository;

    public Item validateItemId(long itemId) {
        if (itemId < 0) {
            throw new DataNotFoundException("Не может существовать вещи с номером меньше 0");
        }
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Такого предмета не существует!"));
    }

    public void validateCommentData(CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isEmpty()) {
            throw new DataValidationException("Комментарий не может быть пустым");
        }
    }
}

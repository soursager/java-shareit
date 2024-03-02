package ru.practicum.shareit.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.item.dto.ItemMapper.*;

public class CommentMapperTest {

    private User owner;
    private Item item;

    @BeforeEach
    public void fillData() {
        owner = User.builder()
                .name("name")
                .id(1L)
                .build();
        item = Item.builder()
                .id(1L)
                .name("name")
                .owner(owner)
                .description("desc")
                .available(true)
                .build();
    }

    @Test
    void toComment() {
        CommentDto commentDto = CommentDto.builder()
                .text("text")
                .created(LocalDateTime.now())
                .id(1L)
                .authorName("Bob")
                .item(toItemDto(item))
                .build();

        Comment comment = CommentMapper.toComment(commentDto, owner, item);

        assertEquals(commentDto.getText(), comment.getText());
        assertEquals(comment.getAuthor().getId(), owner.getId());
        assertEquals(comment.getItem().getId(), item.getId());
    }

    @Test
    void toCommentDto() {
        Comment comment = Comment.builder()
                .author(owner)
                .created(LocalDateTime.now())
                .item(item)
                .text("text")
                .id(1L)
                .build();

        CommentDto actual = CommentMapper.toCommentDto(comment);

        assertEquals(actual.getText(), comment.getText());
        assertEquals(actual.getItem().getId(), item.getId());
        assertEquals(actual.getAuthorName(), owner.getName());
    }
}

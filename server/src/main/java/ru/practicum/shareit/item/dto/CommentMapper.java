package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    public static Comment toComment(CommentDto commentDto, User author, Item item) {
        return Comment.builder()
                .id(commentDto.getId() != null ? commentDto.getId() : 0L)
                .author(author)
                .item(item)
                .text(commentDto.getText())
                .created(commentDto.getCreated())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = CommentDto.builder()
                .id(comment.getId())
                .created(comment.getCreated())
                .text(comment.getText())
                .build();
        if (comment.getItem() != null) {
            ItemDto itemDto = ItemMapper.toItemDto(comment.getItem());
            commentDto.setItem(itemDto);
        }
        if (comment.getAuthor() != null) {
            UserDto userDto = UserMapper.toUserDto(comment.getAuthor());
            commentDto.setAuthorName(userDto.getName());
        }
        return commentDto;
    }

    public static List<CommentDto> toCommentsDto(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}

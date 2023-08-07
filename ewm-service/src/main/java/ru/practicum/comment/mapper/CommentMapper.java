package ru.practicum.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {

    public Comment toComment(NewCommentDto newCommentDto, User commentator, Event event) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .commentator(commentator)
                .event(event)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .commentatorId(comment.getCommentator().getId())
                .eventId(comment.getEvent().getId())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }
}

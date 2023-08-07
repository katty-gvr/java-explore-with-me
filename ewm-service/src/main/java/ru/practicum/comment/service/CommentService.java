package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentUpdateDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.util.Collection;

public interface CommentService {

    CommentDto addNewComment(Long userId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, CommentUpdateDto commentUpdateDto);

    void deleteCommentByUser(Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);

    CommentDto getCommentForAdmin(Long commentId);

    Collection<CommentDto> getAllUserCommentsForAdmin(Long userId, Integer from, Integer size);

    Collection<CommentDto> getEventComments(Long eventId, Integer from, Integer size);
}

package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentUpdateDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping("/{userId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addNewComment(@PathVariable final Long userId,
                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.addNewComment(userId, newCommentDto);
    }

    @PatchMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable final Long userId,
                                    @PathVariable final Long commentId,
                                    @Valid @RequestBody CommentUpdateDto commentUpdateDto) {
        return commentService.updateComment(userId, commentId, commentUpdateDto);
    }

    @DeleteMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAuthor(@PathVariable final Long userId,
                                      @PathVariable final Long commentId) {
        commentService.deleteCommentByUser(userId, commentId);
    }
}

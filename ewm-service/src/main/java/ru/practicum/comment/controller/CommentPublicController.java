package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/comments")
public class CommentPublicController {
    private final CommentService commentService;

    @GetMapping("/events/{eventId}")
    public Collection<CommentDto> getEventComments(@PathVariable final Long eventId,
                                                   @RequestParam(value = "from", required = false, defaultValue = "0")
                                                   @PositiveOrZero(message = "Значение 'from' должно быть положительным")
                                                   final Integer from,
                                                   @RequestParam(value = "size", required = false, defaultValue = "10")
                                                   @Positive(message = "Значение 'size' должно быть положительным")
                                                   final Integer size) {

        return commentService.getEventComments(eventId, from, size);
    }
}

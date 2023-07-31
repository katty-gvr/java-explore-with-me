package ru.practicum.comment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    Long id;
    String text;
    Long commentatorId;
    Long eventId;
    LocalDateTime created;
    LocalDateTime updated;
}

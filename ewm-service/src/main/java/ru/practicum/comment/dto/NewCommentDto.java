package ru.practicum.comment.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class NewCommentDto {
    @NotNull
    Long eventId;

    @NotNull
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 1000)
    String text;
}

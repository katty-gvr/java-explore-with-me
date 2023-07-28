package ru.practicum.compilation.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
public class CompilationUpdateDto {
    Set<Long> events;
    Boolean pinned;
    @Size(min = 1, max = 50)
    String title;
}

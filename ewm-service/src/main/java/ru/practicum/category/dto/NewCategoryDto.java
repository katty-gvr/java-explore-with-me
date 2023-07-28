package ru.practicum.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryDto {
    @NotNull(message = "Имя категории должно быть задано")
    @NotBlank(message = "Имя категории не может быть пустым")
    @Size(max = 50, message = "Имя категории не должно превышать 50 символов")
    String name;
}

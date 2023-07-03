package ru.practicum.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class UserDto {
    Long id;
    @NotNull(message = "Email пользователя должен быть задан")
    @NotBlank(message = "Email пользователя не может быть пустым")
    @Email
    @Size(min = 6, max = 254)
    String email;
    @NotNull(message = "Имя пользователя должно быть задано")
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 250)
    String name;
}

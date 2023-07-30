package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class LocationDto {
    @NotNull(message = "Широта должна быть задана!")
    private Float lat;
    @NotNull(message = "Долгота должна быть задана!")
    private Float lon;
}

package ru.practicum.server.mapper;

import ru.practicum.dto.StatDto;
import ru.practicum.server.model.Stat;

public class StatMapper {
    public static StatDto toStatDto(Stat stat) {
        return StatDto.builder()
                .app(stat.getApp())
                .uri(stat.getUri())
                .hits(stat.getHits())
                .build();
    }
}

package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto compilationDto, Set<Event> events) {
        return Compilation.builder()
                .pinned(compilationDto.getPinned() != null ? compilationDto.getPinned() : false)
                .title(compilationDto.getTitle())
                .events(events != null ? events : Collections.emptySet())
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .events(compilation.getEvents() != null && !compilation.getEvents().isEmpty() ?
                        EventMapper.toEventDtoSet(compilation.getEvents()) : Collections.emptySet())
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static Collection<CompilationDto> toCompilationDtoList(Page<Compilation> compilationPage) {
        return compilationPage.stream().map(CompilationMapper::toCompilationDto).collect(Collectors.toList());
    }
}

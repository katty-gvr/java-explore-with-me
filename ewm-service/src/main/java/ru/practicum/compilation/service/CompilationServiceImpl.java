package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationUpdateDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.*;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> compilationEvents = new HashSet<>();

        if (newCompilationDto.getEvents() != null) {
            compilationEvents = getEventsOfCompilation(newCompilationDto.getEvents());
        }
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, compilationEvents);

        log.info("Добавлена новая подборка {}", compilation.getTitle());
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        log.info("Получена подборка событий по id {}", compilationId);
        return CompilationMapper.toCompilationDto(getCompilation(compilationId));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationInfo(Long compilationId, CompilationUpdateDto newCompilationInfo) {
        Compilation compilation = getCompilation(compilationId);
        if (newCompilationInfo.getPinned() != null) {
            compilation.setPinned(newCompilationInfo.getPinned());
        }
        if (newCompilationInfo.getTitle() != null && !newCompilationInfo.getTitle().isBlank()) {
            compilation.setTitle(newCompilationInfo.getTitle());
        }
        if (newCompilationInfo.getEvents() != null) {
            compilation.setEvents(getEventsOfCompilation(newCompilationInfo.getEvents()));
        }
        log.info("Подборка с id {} успешно обновлена", compilationId);
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        getCompilation(compilationId);
        compilationRepository.deleteById(compilationId);
        log.info("Подоборка с id {} удалена", compilationId);
    }

    @Override
    public Collection<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        log.info("Получен список подборок с параметрами pinned {}, from {}, size {}", pinned, from, size);
        return pinned != null ? CompilationMapper.toCompilationDtoList(compilationRepository.findAllByPinnedEquals(pinned, pageable))
                : CompilationMapper.toCompilationDtoList(compilationRepository.findAll(pageable));
    }

    private Compilation getCompilation(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException(String.format("Подборка с id = %d не найдена", compilationId)));
    }

    private Set<Event> getEventsOfCompilation(Set<Long> eventIds) {
        List<Event> events = eventRepository.findAllById(eventIds);
        return new HashSet<>(events);
    }
}

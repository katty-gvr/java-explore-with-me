package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationUpdateDto;
import ru.practicum.compilation.dto.NewCompilationDto;

import java.util.Collection;

public interface CompilationService {

    CompilationDto addNewCompilation(NewCompilationDto newCompilationDto);

    CompilationDto getCompilationById(Long compilationId);

    CompilationDto updateCompilationInfo(Long compilationId, CompilationUpdateDto newCompilationInfo);

    void deleteCompilation(Long compilationId);

    Collection<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size);
}

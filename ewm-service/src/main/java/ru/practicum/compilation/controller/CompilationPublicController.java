package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/compilations")
public class CompilationPublicController {

    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilationById(@PathVariable final Long compId) {
        return compilationService.getCompilationById(compId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                         @RequestParam(value = "from", required = false, defaultValue = "0")
                                                         @PositiveOrZero(message = "Значение 'from' должно быть положительным")
                                                         final Integer from,
                                                         @RequestParam(value = "size", required = false, defaultValue = "10")
                                                         @Positive(message = "Значение 'size' должно быть положительным")
                                                         final Integer size) {

        return compilationService.getAllCompilations(pinned, from, size);
    }
}

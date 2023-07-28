package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addNewCategory(NewCategoryDto newCategoryDto) {
        Category categoryForSave = CategoryMapper.toCategory(newCategoryDto);
        log.info("Добавлена новая категория {}", categoryForSave.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryForSave));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, NewCategoryDto newCategoryDto) {
        Category categoryForUpdate = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Категория с id=%d не найдена", categoryId)));
        categoryForUpdate.setName(newCategoryDto.getName());
        log.info("Обновлена категория {}", newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryForUpdate));
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException(String.format("Категория с id=%d не найдена", catId)));

        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConflictException("Невозможно удалить категорию, так как к ней привязаны события!");
        } else {
            categoryRepository.deleteById(catId);
            log.info("Удалена категория с id {}", catId);
        }
    }

    @Override
    public Collection<CategoryDto> getAllCategories(Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("id").ascending());
        Page<Category> categories = categoryRepository.findAll(page);
        if (categories.isEmpty()) {
            return Collections.emptyList();
        } else {
            log.info("Получены категории {}", categories);
            return categories.stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
        }
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException(String.format("Категория с id=%d не найдена", catId)));
        log.info("Получена категория с id = {}", catId);
        return CategoryMapper.toCategoryDto(category);
    }
}

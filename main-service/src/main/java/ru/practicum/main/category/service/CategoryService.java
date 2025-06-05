package ru.practicum.main.category.service;

import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(Long catId, CategoryDto dto);

    void deleteCategory(Long catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);
}

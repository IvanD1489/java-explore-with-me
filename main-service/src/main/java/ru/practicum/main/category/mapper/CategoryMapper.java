package ru.practicum.main.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.model.Category;

@Component
public class CategoryMapper {
    public Category toCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}

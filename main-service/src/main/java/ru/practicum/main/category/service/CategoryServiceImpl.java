package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Category name must be unique");
        }
        Category category = categoryMapper.toCategory(dto);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Category name must be unique");
        }
        category.setName(dto.getName());
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Category is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size, Sort.by("id")))
                .stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return categoryMapper.toCategoryDto(category);
    }
}

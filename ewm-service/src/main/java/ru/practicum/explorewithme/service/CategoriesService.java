package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoriesService {

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategoryById(Long catId);

    CategoryDto createNewCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategoryById(Long catId, NewCategoryDto newCategoryDto);

    void deleteCategoryById(Long catId);
}

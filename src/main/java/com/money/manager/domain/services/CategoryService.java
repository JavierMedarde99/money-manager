package com.money.manager.domain.services;

import java.util.List;

import com.money.manager.domain.User;
import com.money.manager.infrastructure.dtos.CategoryRequestDTO;
import com.money.manager.infrastructure.dtos.CategoryResponseDTO;
import com.money.manager.domain.Category;

public interface CategoryService {
    List<CategoryResponseDTO> getCategoryByUser(User user);
    CategoryResponseDTO getCategory(Long categoryId);
    CategoryResponseDTO createCategory(CategoryRequestDTO categoryDto, User user);
    CategoryResponseDTO updateCategory(CategoryRequestDTO categoryDto, Long categoryId, User user);
    String deleteCartegory(Long category);
}

package com.money.manager.domain.services;

import java.util.List;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.infrastructure.dtos.CategoryRequestDTO;
import com.money.manager.infrastructure.dtos.CategoryResponseDTO;

public interface CategoryService {
    List<CategoryResponseDTO> getCategoryByUser(User user);
    CategoryResponseDTO getCategory(Long categoryId, User user) throws NotFoundException;
    CategoryResponseDTO createCategory(CategoryRequestDTO categoryDto, User user);
    CategoryResponseDTO updateCategory(CategoryRequestDTO categoryDto, Long categoryId, User user) throws NotFoundException;
    String deleteCartegory(Long category, User user) throws NotFoundException;
}

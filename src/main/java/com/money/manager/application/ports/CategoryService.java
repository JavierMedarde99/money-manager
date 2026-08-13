package com.money.manager.application.ports;

import java.util.List;

import com.money.manager.application.dtos.CategoryRequestDTO;
import com.money.manager.application.dtos.CategoryResponseDTO;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;

public interface CategoryService {
    List<CategoryResponseDTO> getCategoryByUser(User user);
    CategoryResponseDTO getCategory(Long categoryId) throws NotFoundException;
    CategoryResponseDTO createCategory(CategoryRequestDTO categoryDto, User user);
    CategoryResponseDTO updateCategory(CategoryRequestDTO categoryDto, Long categoryId, User user) throws NotFoundException;
    String deleteCartegory(Long category) throws NotFoundException;
}

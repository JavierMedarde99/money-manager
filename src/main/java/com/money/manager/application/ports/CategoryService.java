package com.money.manager.application.ports;

import com.money.manager.domain.Category;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.application.dtos.CategoryRequestDTO;
import com.money.manager.application.dtos.CategoryResponseDTO;

public interface CategoryService {
    Page<CategoryResponseDTO> getCategoryByUser(User user, Pageable pageable);
    CategoryResponseDTO getCategory(Long categoryId, User user) throws NotFoundException;
    Category findCategory(Long categoryId, User user) throws NotFoundException;
    CategoryResponseDTO createCategory(CategoryRequestDTO categoryDto, User user);
    Category findOrCreatePaymentCategory(User user);
    CategoryResponseDTO updateCategory(CategoryRequestDTO categoryDto, Long categoryId, User user) throws NotFoundException;
    String deleteCategory(Long category, User user) throws NotFoundException;
}

package com.money.manager.application.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.CategoryMapper;
import com.money.manager.domain.Category;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.CategoryService;
import com.money.manager.infrastructure.dtos.CategoryRequestDTO;
import com.money.manager.infrastructure.dtos.CategoryResponseDTO;
import com.money.manager.infrastructure.persistance.PostgresCategoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImp implements CategoryService {

    private final PostgresCategoryRepository categoryRepository;

    @Override
    public List<CategoryResponseDTO> getCategoryByUser(User user) {
        List<Category> getAllCategory = categoryRepository.findByUser(user);
        return getAllCategory.stream().map(category -> CategoryMapper.toDto(category)).toList();
    }

    @Override
    public CategoryResponseDTO getCategory(Long categoryId, User user) throws NotFoundException {
        Category category = findCategoryById(categoryId, user);
        return CategoryMapper.toDto(category);
    }

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryDTO, User user) {
        Category category = CategoryMapper.fromDto(categoryDTO, user);
        categoryRepository.save(category);
        return CategoryMapper.toDto(category);

    }

    @Transactional
    @Override
    public CategoryResponseDTO updateCategory(CategoryRequestDTO categoryDTO, Long categoryId, User user)
            throws NotFoundException {
        Category category = findCategoryById(categoryId, user);
        category.setName(categoryDTO.name());
        category.setColor(categoryDTO.color());
        categoryRepository.save(category);
        return CategoryMapper.toDto(category);
    }

    @Override
    public String deleteCartegory(Long categoryId, User user) throws NotFoundException{
        Category category = findCategoryById(categoryId, user);
        categoryRepository.delete(category);
        return "category delete";
    }


    private Category findCategoryById(Long categoryId, User user) throws NotFoundException{
        Optional<Category> optCategory = categoryRepository.findByIdAndUser_Id(categoryId, user.getId());
        return optCategory.orElseThrow(() -> new NotFoundException("category not found"));
    }

}

package com.money.manager.application.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.CategoryMapper;
import com.money.manager.domain.Category;
import com.money.manager.domain.CategoryRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.domain.paging.SortDirection;
import com.money.manager.application.ports.CategoryService;
import com.money.manager.application.dtos.CategoryRequestDTO;
import com.money.manager.application.dtos.CategoryResponseDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImp implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryResponseDTO> getCategoryByUser(User user, Pageable pageable) {
        Sort sort = pageable.direction() == SortDirection.DESC
                ? Sort.by(sortProperty(pageable.sortBy())).descending()
                : Sort.by(sortProperty(pageable.sortBy())).ascending();

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(
                pageable.page(), pageable.size(), sort);

        org.springframework.data.domain.Page<Category> categories = categoryRepository.findByUser(user, springPageable);

        List<CategoryResponseDTO> content = categories.getContent().stream()
                .map(CategoryMapper::toDto).toList();

        return Page.of(content, categories.getNumber(), categories.getSize(),
                categories.getTotalElements(), categories.getTotalPages());
    }

    private static String sortProperty(String sortBy) {
        return switch (sortBy) {
            case "name" -> "name";
            case "color" -> "color";
            default -> "id";
        };
    }

    @Override
    public CategoryResponseDTO getCategory(Long categoryId, User user) throws NotFoundException {
        Category category = findCategoryById(categoryId, user);
        return CategoryMapper.toDto(category);
    }

    @Override
    public Category findCategory(Long categoryId, User user) throws NotFoundException {
        return findCategoryById(categoryId, user);
    }

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryDTO, User user) {
        Category category = CategoryMapper.fromDto(categoryDTO, user);
        category = categoryRepository.save(category);
        return CategoryMapper.toDto(category);
    }

    private static final String PAYMENT_CATEGORY_NAME = "pago";
    private static final String PAYMENT_CATEGORY_COLOR = "#000000";

    @Override
    public Category findOrCreatePaymentCategory(User user) {
        return categoryRepository.findByNameAndUser_Id(PAYMENT_CATEGORY_NAME, user.getId())
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name(PAYMENT_CATEGORY_NAME)
                                .color(PAYMENT_CATEGORY_COLOR)
                                .user(user)
                                .build()));
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
    public String deleteCategory(Long categoryId, User user) throws NotFoundException{
        Category category = findCategoryById(categoryId, user);
        categoryRepository.delete(category);
        return "category delete";
    }


    private Category findCategoryById(Long categoryId, User user) throws NotFoundException{
        Optional<Category> optCategory = categoryRepository.findByIdAndUser_Id(categoryId, user.getId());
        return optCategory.orElseThrow(() -> new NotFoundException("category not found"));
    }

}

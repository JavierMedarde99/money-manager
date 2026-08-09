package com.money.manager.application.mappers;

import com.money.manager.domain.Category;
import com.money.manager.domain.User;
import com.money.manager.infrastructure.dtos.CategoryRequestDTO;
import com.money.manager.infrastructure.dtos.CategoryResponseDTO;

public class CategoryMapper {
    
    public static Category fromDto(final CategoryRequestDTO categoryRequestDTO, User user){
        return Category.builder().name(categoryRequestDTO.name()).color(categoryRequestDTO.color()).user(user).build();
    }

    public static CategoryResponseDTO toDto(final Category category){
        return new CategoryResponseDTO(category.getName(),category.getColor(),category.getId());
    }

    public static Category fromDto(final CategoryResponseDTO categoryResponseDTO,User user){
        return Category.builder().name(categoryResponseDTO.name()).color(categoryResponseDTO.color()).user(user).id(categoryResponseDTO.id()).build();
    }
}

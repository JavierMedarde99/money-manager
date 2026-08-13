package com.money.manager.application.mappers;

import com.money.manager.domain.Category;
import com.money.manager.domain.User;
import com.money.manager.application.dtos.CategoryRequestDTO;
import com.money.manager.application.dtos.CategoryResponseDTO;

public class CategoryMapper {
    
    public static Category fromDto(final CategoryRequestDTO categoryRequestDTO, User user){
        return Category.builder().name(categoryRequestDTO.name()).color(categoryRequestDTO.color()).user(user).build();
    }

    public static CategoryResponseDTO toDto(final Category category){
        return new CategoryResponseDTO(category.getId(),category.getName(),category.getColor());
    }

    public static Category fromDto(final CategoryResponseDTO categoryResponseDTO,User user){
        return Category.builder().name(categoryResponseDTO.name()).color(categoryResponseDTO.color()).user(user).id(categoryResponseDTO.id()).build();
    }
}

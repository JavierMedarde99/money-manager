package com.money.manager.infrastructure.persistance.mapper;

import com.money.manager.domain.Category;
import com.money.manager.infrastructure.persistance.entity.CategoryJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

public class CategoryJpaMapper {

    public static CategoryJpa toJpa(Category category, UserJpa userJpa) {
        return CategoryJpa.builder().id(category.getId()).name(category.getName())
                .color(category.getColor()).user(userJpa).build();
    }

    public static Category toDomain(CategoryJpa jpa) {
        return Category.builder().id(jpa.getId()).name(jpa.getName()).color(jpa.getColor())
                .user(UserJpaMapper.toDomain(jpa.getUser())).build();
    }
}

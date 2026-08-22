package com.money.manager.infrastructure.persistance.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.domain.Category;
import com.money.manager.domain.CategoryRepository;
import com.money.manager.infrastructure.persistance.PostgresCategoryRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.entity.UserJpa;
import com.money.manager.infrastructure.persistance.mapper.CategoryJpaMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final PostgresCategoryRepository jpa;
    private final PostgresUserRepository jpaUser;

    @Override
    @Transactional(readOnly = true)
    public List<Category> findByUser(com.money.manager.domain.User user) {
        return jpa.findByUser_Id(user.getId()).stream().map(CategoryJpaMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return jpa.findById(id).map(CategoryJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByIdAndUser_Id(Long id, Long userId) {
        return jpa.findByIdAndUser_Id(id, userId).map(CategoryJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public Category save(Category category) {
        UserJpa userJpa = jpaUser.findById(category.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("user not found"));
        return CategoryJpaMapper.toDomain(jpa.save(CategoryJpaMapper.toJpa(category, userJpa)));
    }

    @Override
    @Transactional
    public void delete(Category category) {
        jpa.deleteById(category.getId());
    }
}

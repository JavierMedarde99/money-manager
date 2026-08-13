package com.money.manager.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findByUser(User user);

    Optional<Category> findById(Long id);

    Optional<Category> findByIdAndUser_Id(Long id, Long userId);

    Category save(Category category);

    void delete(Category category);
}

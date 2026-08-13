package com.money.manager.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findByUser(User user);

    Optional<Category> findByIdAndUser_Id(Long id, Long userId);
}

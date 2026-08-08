package com.money.manager.domain;

import java.util.List;

public interface CategoryRepository {
    List<Category> findByuserId(User user);
}

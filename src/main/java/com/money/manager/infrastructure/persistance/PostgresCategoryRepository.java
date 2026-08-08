package com.money.manager.infrastructure.persistance;


import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.domain.Category;
import com.money.manager.domain.CategoryRepository;

public interface PostgresCategoryRepository extends JpaRepository<Category,Long>, CategoryRepository{
    
}

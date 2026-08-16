package com.money.manager.infrastructure.persistance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.infrastructure.persistance.entity.CategoryJpa;

public interface PostgresCategoryRepository extends JpaRepository<CategoryJpa, Long> {
    List<CategoryJpa> findByUser_Id(Long userId);

    Optional<CategoryJpa> findByIdAndUser_Id(Long id, Long userId);
}

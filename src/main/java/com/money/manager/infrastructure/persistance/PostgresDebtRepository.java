package com.money.manager.infrastructure.persistance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.infrastructure.persistance.entity.DebtJpa;

public interface PostgresDebtRepository extends JpaRepository<DebtJpa, Long> {
    List<DebtJpa> findByUser_Id(Long userId);

    Optional<DebtJpa> findByIdAndUser_Id(Long id, Long userId);
}

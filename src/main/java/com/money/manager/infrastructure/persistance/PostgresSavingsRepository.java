package com.money.manager.infrastructure.persistance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.infrastructure.persistance.entity.SavingsJpa;

public interface PostgresSavingsRepository extends JpaRepository<SavingsJpa, Long> {

    List<SavingsJpa> findByUser_IdOrderByYearAscMonthAsc(Long userId);

    Optional<SavingsJpa> findByUser_IdAndYearAndMonth(Long userId, int year, int month);

    void deleteByUser_Id(Long userId);
}

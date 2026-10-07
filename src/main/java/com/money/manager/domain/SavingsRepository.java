package com.money.manager.domain;

import java.util.List;
import java.util.Optional;

public interface SavingsRepository {
    List<Savings> findByUser(User user);

    Optional<Savings> findByUserAndYearAndMonth(User user, int year, int month);

    Savings save(Savings savings);

    void delete(Savings savings);

    void deleteByUser_Id(Long userId);
}

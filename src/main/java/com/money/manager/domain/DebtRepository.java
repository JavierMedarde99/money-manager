package com.money.manager.domain;

import java.util.List;
import java.util.Optional;

public interface DebtRepository {
    List<Debt> findByUser(User user);

    Optional<Debt> findById(Long id);

    Optional<Debt> findByIdAndUser_Id(Long id, Long userId);

    Debt save(Debt debt);

    void delete(Debt debt);
}

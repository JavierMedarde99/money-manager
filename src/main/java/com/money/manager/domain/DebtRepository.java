package com.money.manager.domain;

import java.util.List;
import java.util.Optional;

public interface DebtRepository {
    List<Debt> findByUser(User user);

    Optional<Debt> findById(Long id);

    Debt save(Debt debt);

    void delete(Debt debt);
}

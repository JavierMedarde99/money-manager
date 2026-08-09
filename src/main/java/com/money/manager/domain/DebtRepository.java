package com.money.manager.domain;

import java.util.List;

public interface DebtRepository {
    List<Debt> findByUser(User user);
}

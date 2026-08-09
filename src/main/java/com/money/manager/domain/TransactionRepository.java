package com.money.manager.domain;

import java.util.List;

public interface TransactionRepository {
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserAndCategory(User user,Category category);
}

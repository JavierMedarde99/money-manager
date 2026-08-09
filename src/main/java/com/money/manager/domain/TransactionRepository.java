package com.money.manager.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;

public interface TransactionRepository {
    List<Transaction> findByUser(User user);

    List<Transaction> findByUserAndCategory(User user, Category category);

    Page<Transaction> findByFilters(
            User user,
            Type type,
            Subtype subtype,
            LocalDate from,
            LocalDate to,
            Pageable pageable);
}

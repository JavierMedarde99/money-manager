package com.money.manager.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;

public interface TransactionRepository {
    List<Transaction> findByUser(User user);

    List<Transaction> findByUserAndCategory(User user, Category category);

    Optional<Transaction> findById(Long id);

    Optional<Transaction> findByIdAndUser_Id(Long id, Long userId);

    Transaction save(Transaction transaction);

    void delete(Transaction transaction);

    Page<Transaction> findByFilters(
            User user,
            Type type,
            Subtype subtype,
            LocalDate from,
            LocalDate to,
            Pageable pageable);
}

package com.money.manager.infrastructure.persistance;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;

public interface PostgresTransactionRepository extends JpaRepository<Transaction, Long>, TransactionRepository {

    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.user = :user
          AND (:type IS NULL OR t.type = :type)
          AND (:subtype IS NULL OR t.subtype = :subtype)
          AND (CAST(:from AS date) IS NULL OR t.dateTransaction >= :from)
          AND (CAST(:to AS date) IS NULL OR t.dateTransaction <= :to)
    """)
    Page<Transaction> findByFilters(
            @Param("user") User user,
            @Param("type") Type type,
            @Param("subtype") Subtype subtype,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);
}

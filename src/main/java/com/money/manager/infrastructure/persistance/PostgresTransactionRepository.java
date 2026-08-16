package com.money.manager.infrastructure.persistance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;
import com.money.manager.infrastructure.persistance.entity.CategoryJpa;
import com.money.manager.infrastructure.persistance.entity.TransactionJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

public interface PostgresTransactionRepository extends JpaRepository<TransactionJpa, Long> {

    List<TransactionJpa> findByUser_Id(Long userId);

    Optional<TransactionJpa> findByIdAndUser_Id(Long id, Long userId);

    List<TransactionJpa> findByUser_IdAndCategory_Id(Long userId, Long categoryId);

    @Query("""
        SELECT t
        FROM TransactionJpa t
        WHERE t.user = :user
          AND (:type IS NULL OR t.type = :type)
          AND (:subtype IS NULL OR t.subtype = :subtype)
          AND (CAST(:from AS date) IS NULL OR t.dateTransaction >= :from)
          AND (CAST(:to AS date) IS NULL OR t.dateTransaction <= :to)
    """)
    Page<TransactionJpa> findByFilters(
            @Param("user") UserJpa user,
            @Param("type") Type type,
            @Param("subtype") Subtype subtype,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);
}

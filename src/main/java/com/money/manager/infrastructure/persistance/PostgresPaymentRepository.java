package com.money.manager.infrastructure.persistance;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.money.manager.infrastructure.persistance.entity.DebtJpa;
import com.money.manager.infrastructure.persistance.entity.PaymentJpa;

public interface PostgresPaymentRepository extends JpaRepository<PaymentJpa, Long> {

    Optional<PaymentJpa> findByIdAndDebt_User_Id(Long id, Long userId);

    void deleteByDebt_User_Id(Long userId);

    List<PaymentJpa> findByAutomaticPaymentTrueAndDebt_EndDateIsNull();

    @Query("""
        SELECT COUNT(p) > 0
        FROM PaymentJpa p
        WHERE p.debt = :debt
          AND p.amount = :amount
          AND EXTRACT(YEAR FROM p.paymentDate) = :year
          AND EXTRACT(MONTH FROM p.paymentDate) = :month
    """)
    boolean existsByDebtAmountAndMonth(
            @Param("debt") DebtJpa debt,
            @Param("amount") Double amount,
            @Param("year") int year,
            @Param("month") int month);
}

package com.money.manager.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepository {
    Optional<Payment> findById(Long id);

    Optional<Payment> findByIdAndDebt_User_Id(Long id, Long userId);

    Page<Payment> findByDebt_Id(Long debtId, Pageable pageable);

    long countByDebt_Id(Long debtId);

    List<Payment> findAutomaticPaymentsForOpenDebts();

    boolean existsByDebtAmountAndMonth(Debt debt, Double amount, int year, int month);

    Payment save(Payment payment);

    void delete(Payment payment);
}

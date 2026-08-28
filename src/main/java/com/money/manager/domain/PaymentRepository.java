package com.money.manager.domain;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(Long id);

    Optional<Payment> findByIdAndDebt_User_Id(Long id, Long userId);

    List<Payment> findAutomaticPaymentsForOpenDebts();

    boolean existsByDebtAmountAndMonth(Debt debt, Double amount, int year, int month);

    Payment save(Payment payment);

    void delete(Payment payment);
}

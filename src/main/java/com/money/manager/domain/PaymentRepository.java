package com.money.manager.domain;

import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(Long id);

    Optional<Payment> findByIdAndDebt_User_Id(Long id, Long userId);

    Payment save(Payment payment);

    void delete(Payment payment);
}

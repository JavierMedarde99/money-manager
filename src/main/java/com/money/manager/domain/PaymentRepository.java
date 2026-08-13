package com.money.manager.domain;

import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(Long id);

    Payment save(Payment payment);

    void delete(Payment payment);
}

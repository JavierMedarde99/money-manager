package com.money.manager.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.infrastructure.persistance.entity.PaymentJpa;

public interface PostgresPaymentRepository extends JpaRepository<PaymentJpa, Long> {
}

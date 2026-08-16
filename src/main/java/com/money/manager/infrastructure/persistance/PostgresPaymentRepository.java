package com.money.manager.infrastructure.persistance;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.infrastructure.persistance.entity.PaymentJpa;

public interface PostgresPaymentRepository extends JpaRepository<PaymentJpa, Long> {

    Optional<PaymentJpa> findByIdAndDebt_User_Id(Long id, Long userId);
}

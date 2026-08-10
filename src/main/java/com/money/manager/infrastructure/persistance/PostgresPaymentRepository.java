package com.money.manager.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.domain.Payment;
import com.money.manager.domain.PaymentReposiory;

public interface PostgresPaymentRepository extends JpaRepository<Payment,Long>,PaymentReposiory{
    
}

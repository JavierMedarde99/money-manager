package com.money.manager.infrastructure.persistance.mapper;

import com.money.manager.domain.Payment;
import com.money.manager.infrastructure.persistance.entity.DebtJpa;
import com.money.manager.infrastructure.persistance.entity.PaymentJpa;

public class PaymentJpaMapper {

    public static PaymentJpa toJpa(Payment payment, DebtJpa debtJpa) {
        return PaymentJpa.builder().id(payment.getId()).paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount()).debt(debtJpa).build();
    }

    public static Payment toDomain(PaymentJpa jpa) {
        return Payment.builder().id(jpa.getId()).paymentDate(jpa.getPaymentDate())
                .amount(jpa.getAmount())
                .debt(jpa.getDebt() == null ? null
                        : com.money.manager.domain.Debt.builder().id(jpa.getDebt().getId()).build())
                .build();
    }
}

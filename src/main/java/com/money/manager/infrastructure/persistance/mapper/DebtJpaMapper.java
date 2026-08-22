package com.money.manager.infrastructure.persistance.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.money.manager.domain.Debt;
import com.money.manager.domain.Payment;
import com.money.manager.infrastructure.persistance.entity.DebtJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

public class DebtJpaMapper {

    public static DebtJpa toJpa(Debt debt, UserJpa userJpa) {
        DebtJpa debtJpa = DebtJpa.builder().id(debt.getId()).name(debt.getName())
                .totalAmount(debt.getTotalAmount()).startDate(debt.getStartDate())
                .endDate(debt.getEndDate()).user(userJpa).build();

        if (debt.getPayments() != null) {
            debtJpa.setPayments(debt.getPayments().stream()
                    .map(payment -> PaymentJpaMapper.toJpa(payment, debtJpa))
                    .collect(Collectors.toSet()));
        }
        return debtJpa;
    }

    public static Debt toDomain(DebtJpa jpa) {
        Set<Payment> payments = jpa.getPayments() == null ? Set.of()
                : jpa.getPayments().stream().map(PaymentJpaMapper::toDomain).collect(Collectors.toSet());

        return Debt.builder().id(jpa.getId()).name(jpa.getName()).totalAmount(jpa.getTotalAmount())
                .startDate(jpa.getStartDate()).endDate(jpa.getEndDate())
                .user(UserJpaMapper.toDomain(jpa.getUser())).payments(payments).build();
    }
}

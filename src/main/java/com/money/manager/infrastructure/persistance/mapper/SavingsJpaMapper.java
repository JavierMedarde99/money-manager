package com.money.manager.infrastructure.persistance.mapper;

import com.money.manager.domain.Savings;
import com.money.manager.domain.User;
import com.money.manager.infrastructure.persistance.entity.SavingsJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

public class SavingsJpaMapper {

    public static SavingsJpa toJpa(Savings savings, UserJpa userJpa) {
        return SavingsJpa.builder().id(savings.getId()).year(savings.getYear()).month(savings.getMonth())
                .totalIncome(savings.getTotalIncome()).totalExpense(savings.getTotalExpense())
                .savings(savings.getSavings()).user(userJpa).build();
    }

    public static Savings toDomain(SavingsJpa jpa) {
        return Savings.builder().id(jpa.getId()).year(jpa.getYear()).month(jpa.getMonth())
                .totalIncome(jpa.getTotalIncome()).totalExpense(jpa.getTotalExpense())
                .savings(jpa.getSavings())
                .user(jpa.getUser() == null ? null : User.builder().id(jpa.getUser().getId()).build())
                .build();
    }
}

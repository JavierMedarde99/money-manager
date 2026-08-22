package com.money.manager.infrastructure.persistance.mapper;

import com.money.manager.domain.Transaction;
import com.money.manager.infrastructure.persistance.entity.CategoryJpa;
import com.money.manager.infrastructure.persistance.entity.TransactionJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

public class TransactionJpaMapper {

    public static TransactionJpa toJpa(Transaction transaction, UserJpa userJpa, CategoryJpa categoryJpa) {
        return TransactionJpa.builder().id(transaction.getId()).name(transaction.getName())
                .dateTransaction(transaction.getDateTransaction()).amount(transaction.getAmount())
                .price(transaction.getPrice()).type(transaction.getType()).subtype(transaction.getSubtype())
                .user(userJpa).category(categoryJpa).build();
    }

    public static Transaction toDomain(TransactionJpa jpa) {
        return Transaction.builder().id(jpa.getId()).name(jpa.getName())
                .dateTransaction(jpa.getDateTransaction()).amount(jpa.getAmount())
                .price(jpa.getPrice()).type(jpa.getType()).subtype(jpa.getSubtype())
                .user(UserJpaMapper.toDomain(jpa.getUser()))
                .category(CategoryJpaMapper.toDomain(jpa.getCategory()))
                .build();
    }
}

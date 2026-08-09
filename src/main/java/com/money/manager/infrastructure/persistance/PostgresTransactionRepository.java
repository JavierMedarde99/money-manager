package com.money.manager.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;

public interface PostgresTransactionRepository extends JpaRepository<Transaction,Long>,TransactionRepository{
    
}

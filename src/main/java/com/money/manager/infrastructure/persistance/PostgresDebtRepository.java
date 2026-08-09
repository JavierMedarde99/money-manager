package com.money.manager.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;

public interface PostgresDebtRepository extends JpaRepository<Debt,Long>,DebtRepository{
    
}

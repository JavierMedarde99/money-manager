package com.money.manager.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.domain.User;
import com.money.manager.domain.UserRepository;

public interface PostgresUserRespository extends JpaRepository<User,Long>, UserRepository{
    
}

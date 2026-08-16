package com.money.manager.infrastructure.persistance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.manager.infrastructure.persistance.entity.UserJpa;

public interface PostgresUserRepository extends JpaRepository<UserJpa, Long> {
    Optional<UserJpa> findByUsername(String username);
}

package com.money.manager.infrastructure.persistance.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.domain.User;
import com.money.manager.domain.UserRepository;
import com.money.manager.infrastructure.persistance.PostgresCategoryRepository;
import com.money.manager.infrastructure.persistance.PostgresDebtRepository;
import com.money.manager.infrastructure.persistance.PostgresPaymentRepository;
import com.money.manager.infrastructure.persistance.PostgresTransactionRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.mapper.UserJpaMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final PostgresUserRepository jpa;
    private final PostgresCategoryRepository jpaCategory;
    private final PostgresTransactionRepository jpaTransaction;
    private final PostgresDebtRepository jpaDebt;
    private final PostgresPaymentRepository jpaPayment;

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(UserJpaMapper::toDomain);
    }

    @Override
    public User save(User user) {
        return UserJpaMapper.toDomain(jpa.save(UserJpaMapper.toJpa(user)));
    }

    @Override
    @Transactional
    public void delete(User user) {
        jpaPayment.deleteByDebt_User_Id(user.getId());
        jpaDebt.deleteByUser_Id(user.getId());
        jpaTransaction.deleteByUser_Id(user.getId());
        jpaCategory.deleteByUser_Id(user.getId());
        jpa.deleteById(user.getId());
    }
}
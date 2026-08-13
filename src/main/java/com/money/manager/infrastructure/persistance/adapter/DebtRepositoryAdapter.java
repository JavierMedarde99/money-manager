package com.money.manager.infrastructure.persistance.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;
import com.money.manager.infrastructure.persistance.PostgresDebtRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.entity.UserJpa;
import com.money.manager.infrastructure.persistance.mapper.DebtJpaMapper;
import com.money.manager.infrastructure.persistance.mapper.UserJpaMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DebtRepositoryAdapter implements DebtRepository {

    private final PostgresDebtRepository jpa;
    private final PostgresUserRepository jpaUser;

    @Override
    @Transactional(readOnly = true)
    public List<Debt> findByUser(com.money.manager.domain.User user) {
        return jpa.findByUser_Id(user.getId()).stream().map(DebtJpaMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Debt> findById(Long id) {
        return jpa.findById(id).map(DebtJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public Debt save(Debt debt) {
        UserJpa userJpa = jpaUser.findById(debt.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("user not found"));
        return DebtJpaMapper.toDomain(jpa.save(DebtJpaMapper.toJpa(debt, userJpa)));
    }

    @Override
    @Transactional
    public void delete(Debt debt) {
        jpa.deleteById(debt.getId());
    }
}

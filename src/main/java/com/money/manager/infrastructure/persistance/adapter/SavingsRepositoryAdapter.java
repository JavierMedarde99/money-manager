package com.money.manager.infrastructure.persistance.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.domain.Savings;
import com.money.manager.domain.SavingsRepository;
import com.money.manager.domain.User;
import com.money.manager.infrastructure.persistance.PostgresSavingsRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.entity.SavingsJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;
import com.money.manager.infrastructure.persistance.mapper.SavingsJpaMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SavingsRepositoryAdapter implements SavingsRepository {

    private final PostgresSavingsRepository jpa;
    private final PostgresUserRepository jpaUser;

    @Override
    @Transactional(readOnly = true)
    public List<Savings> findByUser(User user) {
        return jpa.findByUser_IdOrderByYearAscMonthAsc(user.getId()).stream()
                .map(SavingsJpaMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Savings> findByUserAndYearAndMonth(User user, int year, int month) {
        return jpaUser.findById(user.getId())
                .flatMap(userJpa -> jpa.findByUser_IdAndYearAndMonth(userJpa.getId(), year, month))
                .map(SavingsJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public Savings save(Savings savings) {
        UserJpa userJpa = jpaUser.findById(savings.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("user not found"));
        return SavingsJpaMapper.toDomain(jpa.save(SavingsJpaMapper.toJpa(savings, userJpa)));
    }

    @Override
    @Transactional
    public void delete(Savings savings) {
        jpa.deleteById(savings.getId());
    }

    @Override
    @Transactional
    public void deleteByUser_Id(Long userId) {
        jpa.deleteByUser_Id(userId);
    }
}

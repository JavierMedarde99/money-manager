package com.money.manager.infrastructure.persistance.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.money.manager.domain.User;
import com.money.manager.domain.UserRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.mapper.UserJpaMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final PostgresUserRepository jpa;

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(UserJpaMapper::toDomain);
    }

    @Override
    public User save(User user) {
        return UserJpaMapper.toDomain(jpa.save(UserJpaMapper.toJpa(user)));
    }

    @Override
    public void delete(User user) {
        jpa.deleteById(user.getId());
    }
}

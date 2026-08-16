package com.money.manager.infrastructure.persistance.mapper;

import com.money.manager.domain.User;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

public class UserJpaMapper {

    public static UserJpa toJpa(User user) {
        return UserJpa.builder().id(user.getId()).username(user.getUsername())
                .password(user.getPassword()).email(user.getEmail()).build();
    }

    public static User toDomain(UserJpa jpa) {
        return User.builder().id(jpa.getId()).username(jpa.getUsername())
                .password(jpa.getPassword()).email(jpa.getEmail()).build();
    }
}

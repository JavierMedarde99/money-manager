package com.money.manager.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);

    User save(User user);

    void delete(User user);
}

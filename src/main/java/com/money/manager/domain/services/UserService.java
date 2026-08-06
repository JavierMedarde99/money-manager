package com.money.manager.domain.services;

import com.money.manager.domain.User;
import com.money.manager.infrastructure.dtos.LoginRequestDTO;
import com.money.manager.infrastructure.dtos.UserRequestDTO;

public interface UserService {
    String login(LoginRequestDTO loginRequestDTO);
    User getUser(String username);
    String createUser(UserRequestDTO userRequestDtouse);
}

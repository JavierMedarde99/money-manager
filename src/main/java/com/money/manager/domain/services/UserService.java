package com.money.manager.domain.services;

import com.money.manager.domain.User;
import com.money.manager.infrastructure.dtos.LoginRequestDTO;
import com.money.manager.infrastructure.dtos.TokenResponseDTO;
import com.money.manager.infrastructure.dtos.UserRequestDTO;

public interface UserService {
    TokenResponseDTO login(LoginRequestDTO loginRequestDTO);
    User getUser(String username);
    TokenResponseDTO createUser(UserRequestDTO userRequestDtouse);
}

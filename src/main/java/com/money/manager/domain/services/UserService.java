package com.money.manager.domain.services;

import com.money.manager.domain.User;
import com.money.manager.infrastructure.dtos.LoginRequestDTO;
import com.money.manager.infrastructure.dtos.TokenResponseDTO;
import com.money.manager.infrastructure.dtos.UserRequestDTO;
import com.money.manager.infrastructure.dtos.UserResponseDto;

public interface UserService {
    TokenResponseDTO login(LoginRequestDTO loginRequestDTO);
    User getUser(String username);
    TokenResponseDTO createUser(UserRequestDTO userRequestDto);
    UserResponseDto updateUser(UserRequestDTO userRequestDto, User user);
    String deleteUser(User user);
}

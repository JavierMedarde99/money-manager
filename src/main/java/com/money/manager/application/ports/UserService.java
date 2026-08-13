package com.money.manager.application.ports;

import com.money.manager.application.dtos.LoginRequestDTO;
import com.money.manager.application.dtos.TokenResponseDTO;
import com.money.manager.application.dtos.UserRequestDTO;
import com.money.manager.application.dtos.UserResponseDto;
import com.money.manager.domain.User;

public interface UserService {
    TokenResponseDTO login(LoginRequestDTO loginRequestDTO);
    User getUser(String username);
    TokenResponseDTO createUser(UserRequestDTO userRequestDto);
    UserResponseDto updateUser(UserRequestDTO userRequestDto, User user);
    String deleteUser(User user);
}

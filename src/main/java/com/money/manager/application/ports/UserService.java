package com.money.manager.application.ports;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.application.dtos.LoginRequestDTO;
import com.money.manager.application.dtos.TokenResponseDTO;
import com.money.manager.application.dtos.UserRequestDTO;
import com.money.manager.application.dtos.UserResponseDto;

public interface UserService {
    TokenResponseDTO login(LoginRequestDTO loginRequestDTO);
    User getUser(String username) throws NotFoundException;
    TokenResponseDTO createUser(UserRequestDTO userRequestDto);
    UserResponseDto updateUser(UserRequestDTO userRequestDto, User user);
    void deleteUser(User user);
}

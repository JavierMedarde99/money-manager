package com.money.manager.application.mappers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.money.manager.domain.User;
import com.money.manager.infrastructure.dtos.LoginRequestDTO;
import com.money.manager.infrastructure.dtos.UserRequestDTO;

public class UserMapper {
    public static Authentication fromDto(final LoginRequestDTO loginRequestDTO) {
        return new UsernamePasswordAuthenticationToken(loginRequestDTO.username(), loginRequestDTO.password());
    }

    public static User fromDto(final UserRequestDTO userRequestDTO) {
        
        return User.builder().email(userRequestDTO.email()).username(userRequestDTO.username())
                .password(userRequestDTO.password()).build();
    }

    public static LoginRequestDTO toDtoLogin(User user){
        return new LoginRequestDTO(user.getUsername(), user.getPassword());
    }

}

package com.money.manager.application.mappers;

import com.money.manager.domain.User;
import com.money.manager.application.dtos.UserRequestDTO;
import com.money.manager.application.dtos.UserResponseDto;

public class UserMapper {
    public static User fromDto(final UserRequestDTO userRequestDTO) {
        
        return User.builder().email(userRequestDTO.email()).username(userRequestDTO.username())
                .password(userRequestDTO.password()).build();
    }

    public static UserResponseDto toDto(User user){
        return new UserResponseDto(user.getUsername(), user.getEmail());
    }

}

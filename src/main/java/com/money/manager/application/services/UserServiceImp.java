package com.money.manager.application.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.TokenMapper;
import com.money.manager.application.mappers.UserMapper;
import com.money.manager.application.ports.AuthenticationPort;
import com.money.manager.application.ports.TokenService;
import com.money.manager.domain.User;
import com.money.manager.domain.UserRepository;
import com.money.manager.domain.services.UserService;
import com.money.manager.infrastructure.dtos.LoginRequestDTO;
import com.money.manager.infrastructure.dtos.TokenResponseDTO;
import com.money.manager.infrastructure.dtos.UserRequestDTO;
import com.money.manager.infrastructure.dtos.UserResponseDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImp implements UserService, UserDetailsService {

    private final AuthenticationPort authenticationPort;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenResponseDTO login(final LoginRequestDTO loginRequestDTO) {
        authenticationPort.authenticate(loginRequestDTO.username(), loginRequestDTO.password());
        return TokenMapper.toDto(tokenService.generateToken(loginRequestDTO.username()));
    }

    @Override
    public User getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    @Override
    public TokenResponseDTO createUser(final UserRequestDTO userRequestDTO) {
        User user = UserMapper.fromDto(userRequestDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return login(new LoginRequestDTO(user.getUsername(), userRequestDTO.password()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUser(username);
    }

    @Transactional
    @Override
    public UserResponseDto updateUser(UserRequestDTO dto, User user) {

        user.setEmail(dto.email());
        user.setUsername(dto.username());

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        User updatedUser = userRepository.save(user);

        return UserMapper.toDto(updatedUser);
    }

    @Override
    public String deleteUser(User user){
        userRepository.delete(user);
        return "user delete";
    }
}

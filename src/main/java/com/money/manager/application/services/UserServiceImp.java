package com.money.manager.application.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.TokenMapper;
import com.money.manager.application.mappers.UserMapper;
import com.money.manager.domain.User;
import com.money.manager.domain.services.TokenService;
import com.money.manager.domain.services.UserService;
import com.money.manager.infrastructure.dtos.LoginRequestDTO;
import com.money.manager.infrastructure.dtos.TokenResponseDTO;
import com.money.manager.infrastructure.dtos.UserRequestDTO;
import com.money.manager.infrastructure.persistance.PostgresUserRespository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImp implements UserService, UserDetailsService{

    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenService tokenService;
    private final PostgresUserRespository postgresUserRespository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenResponseDTO login(final LoginRequestDTO loginRequestDTO) {
        try {
            final AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
            final Authentication authRequest = UserMapper.fromDto(loginRequestDTO);
            final Authentication authentication = authenticationManager.authenticate(authRequest);

            return TokenMapper.toDto(tokenService.generateToken(authentication));
        } catch (Exception e) {
            log.error("error to try login. Error: {}",e.getMessage(),e);
            throw e;
        }
    }
    
    @Override
    public User getUser(String username){
        return postgresUserRespository.findByUsername(username).orElseThrow();
    }

    @Override
    public TokenResponseDTO createUser(final UserRequestDTO userRequestDTO){
        User user = UserMapper.fromDto(userRequestDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        postgresUserRespository.save(user);
        return login(new LoginRequestDTO(user.getUsername(), userRequestDTO.password()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUser(username);
    }
}

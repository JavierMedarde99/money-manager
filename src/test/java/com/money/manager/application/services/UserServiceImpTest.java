package com.money.manager.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.money.manager.application.dtos.LoginRequestDTO;
import com.money.manager.application.dtos.TokenResponseDTO;
import com.money.manager.application.dtos.UserRequestDTO;
import com.money.manager.application.dtos.UserResponseDto;
import com.money.manager.application.ports.AuthenticationPort;
import com.money.manager.application.ports.TokenService;
import com.money.manager.domain.User;
import com.money.manager.domain.UserRepository;
import com.money.manager.domain.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {

    @Mock
    private AuthenticationPort authenticationPort;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImp userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("javi")
                .password("encoded")
                .email("javi@mail.com")
                .build();
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        when(tokenService.generateToken("javi")).thenReturn("jwt-token");
        when(tokenService.getExpirationSeconds()).thenReturn(30L);

        TokenResponseDTO result = userService.login(new LoginRequestDTO("javi", "secret"));

        verify(authenticationPort).authenticate("javi", "secret");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.expiresIn()).isEqualTo(30L);
    }

    @Test
    void login_withBadCredentials_propagatesExceptionAndDoesNotGenerateToken() {
        org.mockito.Mockito.doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationPort).authenticate("javi", "wrong");

        assertThatThrownBy(() -> userService.login(new LoginRequestDTO("javi", "wrong")))
                .isInstanceOf(BadCredentialsException.class);

        verify(tokenService, org.mockito.Mockito.never()).generateToken(any());
    }

    @Test
    void getUser_withExistingUsername_returnsUser() throws NotFoundException {
        when(userRepository.findByUsername("javi")).thenReturn(Optional.of(user));

        User result = userService.getUser("javi");

        assertThat(result).isSameAs(user);
    }

    @Test
    void getUser_withUnknownUsername_throwsNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser("ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void createUser_encodesPasswordSavesUserAndLogsIn() {
        UserRequestDTO request = new UserRequestDTO("javi", "raw-secret", "javi@mail.com");
        when(passwordEncoder.encode("raw-secret")).thenReturn("{bcrypt}encoded-secret");
        when(tokenService.generateToken("javi")).thenReturn("jwt-token");
        when(tokenService.getExpirationSeconds()).thenReturn(30L);

        TokenResponseDTO result = userService.createUser(request);

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("javi");
        assertThat(saved.getEmail()).isEqualTo("javi@mail.com");
        assertThat(saved.getPassword())
                .isEqualTo("{bcrypt}encoded-secret")
                .isNotEqualTo("raw-secret");

        verify(authenticationPort).authenticate("javi", "raw-secret");
        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void updateUser_withPassword_updatesAllFields() {
        UserRequestDTO request = new UserRequestDTO("new-javi", "new-secret", "new@mail.com");
        when(passwordEncoder.encode("new-secret")).thenReturn("{bcrypt}new-encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.updateUser(request, user);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("{bcrypt}new-encoded");
        assertThat(result.username()).isEqualTo("new-javi");
        assertThat(result.email()).isEqualTo("new@mail.com");
        assertThat(user.getUsername()).isEqualTo("new-javi");
        assertThat(user.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void updateUser_withNullPassword_keepsOldPassword() {
        UserRequestDTO request = new UserRequestDTO("same-javi", null, "same@mail.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUser(request, user);

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("encoded");
        verify(passwordEncoder, org.mockito.Mockito.never()).encode(any());
    }

    @Test
    void updateUser_withBlankPassword_keepsOldPassword() {
        UserRequestDTO request = new UserRequestDTO("same-javi", "   ", "same@mail.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUser(request, user);

        assertThat(user.getPassword()).isEqualTo("encoded");
        verify(passwordEncoder, org.mockito.Mockito.never()).encode(any());
    }

    @Test
    void deleteUser_delegatesToRepository() {
        userService.deleteUser(user);

        verify(userRepository).delete(user);
    }
}

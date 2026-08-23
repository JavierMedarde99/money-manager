package com.money.manager.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.money.manager.application.dtos.LoginRequestDTO;
import com.money.manager.application.dtos.TokenResponseDTO;
import com.money.manager.application.dtos.UserRequestDTO;
import com.money.manager.application.dtos.UserResponseDto;
import com.money.manager.application.ports.UserService;
import com.money.manager.domain.User;
import com.money.manager.infrastructure.config.JwtFilter;
import com.money.manager.infrastructure.security.RateLimiterFilter;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtFilter.class, RateLimiterFilter.class }))
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User principal;

    @BeforeEach
    void setUp() {
        principal = User.builder()
                .id(1L)
                .username("javi")
                .password("encoded")
                .email("javi@mail.com")
                .build();
    }

    @TestConfiguration
    static class PermissiveSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        when(userService.login(new LoginRequestDTO("javi", "secret")))
                .thenReturn(new TokenResponseDTO("Bearer", "jwt-token", 30L));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"javi","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(30));

        ArgumentCaptor<LoginRequestDTO> captor = ArgumentCaptor.forClass(LoginRequestDTO.class);
        verify(userService).login(captor.capture());
        Assertions.assertThat(captor.getValue().username()).isEqualTo("javi");
        Assertions.assertThat(captor.getValue().password()).isEqualTo("secret");
    }

    @Test
    void login_withBlankFields_returns400WithValidationMessage() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("invalid body:")));

        verify(userService, never()).login(any());
    }

    @Test
    void login_withMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The body is not valid"));
    }

    @Test
    void login_withBadCredentials_returns401() throws Exception {
        when(userService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"javi","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void createUser_withValidBody_returnsToken() throws Exception {
        when(userService.createUser(new UserRequestDTO("new-javi", "pass", "new@mail.com")))
                .thenReturn(new TokenResponseDTO("Bearer", "jwt-token", 30L));

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new-javi","password":"pass","email":"new@mail.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));
    }

    @Test
    void createUser_withInvalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new-javi","password":"pass","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    void getUser_returnsPrincipalData() throws Exception {
        mockMvc.perform(get("/user").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("javi"))
                .andExpect(jsonPath("$.email").value("javi@mail.com"));
    }

    @Test
    void updateUser_returnsUpdatedData() throws Exception {
        when(userService.updateUser(any(UserRequestDTO.class), any(User.class)))
                .thenReturn(new UserResponseDto("updated-javi", "updated@mail.com"));

        mockMvc.perform(put("/user").with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"updated-javi","password":null,"email":"updated@mail.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated-javi"))
                .andExpect(jsonPath("$.email").value("updated@mail.com"));
    }

    @Test
    void deleteUser_returns204AndDelegatesToService() throws Exception {
        mockMvc.perform(delete("/user").with(authentication(auth())))
                .andExpect(status().isNoContent())
                .andExpect(content().string(org.hamcrest.Matchers.emptyOrNullString()));

        verify(userService).deleteUser(principal);
    }
}

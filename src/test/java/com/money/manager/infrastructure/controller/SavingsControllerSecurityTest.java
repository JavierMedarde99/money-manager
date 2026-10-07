package com.money.manager.infrastructure.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.money.manager.application.dtos.SavingsResponseDTO;
import com.money.manager.application.ports.SavingsService;
import com.money.manager.application.ports.TokenService;
import com.money.manager.application.ports.UserService;
import com.money.manager.domain.User;
import com.money.manager.domain.UserRepository;
import com.money.manager.infrastructure.config.SecurityConfig;
import com.money.manager.infrastructure.security.RateLimiterService;

/**
 * Pins the real SecurityConfig contract for GET /savings: anonymous requests are
 * rejected (anyRequest().authenticated()) and authenticated requests pass through.
 * Uses the real JwtFilter / RateLimiterFilter with mocked collaborators.
 */
@WebMvcTest(controllers = SavingsController.class)
@Import(SecurityConfig.class)
class SavingsControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavingsService savingsService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @MockitoBean
    private UserRepository userRepository;

    private User principal;

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Test
    void getSavings_withoutAuthentication_rejectsRequest() throws Exception {
        mockMvc.perform(get("/savings"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSavings_withAuthentication_returns200() throws Exception {
        principal = User.builder()
                .id(1L)
                .username("javi")
                .password("encoded")
                .email("javi@mail.com")
                .build();
        when(savingsService.getSavings(principal)).thenReturn(List.of(
                new SavingsResponseDTO(2026, 9, 2500.0, 2100.0, 400.0)));

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        mockMvc.perform(get("/savings").with(authentication(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value(9))
                .andExpect(jsonPath("$[0].savings").value(400.0));
    }
}
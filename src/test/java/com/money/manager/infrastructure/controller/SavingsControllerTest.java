package com.money.manager.infrastructure.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.money.manager.application.dtos.SavingsResponseDTO;
import com.money.manager.application.ports.SavingsService;
import com.money.manager.domain.User;
import com.money.manager.infrastructure.config.JwtFilter;
import com.money.manager.infrastructure.security.RateLimiterFilter;

@WebMvcTest(controllers = SavingsController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtFilter.class, RateLimiterFilter.class }))
class SavingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavingsService savingsService;

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
    void getSavings_returnsListAscending() throws Exception {
        when(savingsService.getSavings(principal)).thenReturn(List.of(
                new SavingsResponseDTO(2026, 8, 3000.0, 2300.0, 700.0),
                new SavingsResponseDTO(2026, 9, 2500.0, 2100.0, 400.0)));

        mockMvc.perform(get("/savings").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2026))
                .andExpect(jsonPath("$[0].month").value(8))
                .andExpect(jsonPath("$[0].totalIncome").value(3000.0))
                .andExpect(jsonPath("$[0].totalExpense").value(2300.0))
                .andExpect(jsonPath("$[0].savings").value(700.0))
                .andExpect(jsonPath("$[1].month").value(9))
                .andExpect(jsonPath("$[1].savings").value(400.0));
    }

    @Test
    void getSavings_emptyHistory_returnsEmptyList() throws Exception {
        when(savingsService.getSavings(principal)).thenReturn(List.of());

        mockMvc.perform(get("/savings").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}

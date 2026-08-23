package com.money.manager.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.money.manager.application.dtos.CategoryResponseDTO;
import com.money.manager.application.dtos.TransactionFilter;
import com.money.manager.application.dtos.TransactionRequestDTO;
import com.money.manager.application.dtos.TransactionResponseDTO;
import com.money.manager.application.ports.TransactionService;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.infrastructure.config.JwtFilter;
import com.money.manager.infrastructure.security.RateLimiterFilter;

@WebMvcTest(controllers = TransactionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtFilter.class, RateLimiterFilter.class }))
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

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

    private TransactionResponseDTO responseDTO() {
        return new TransactionResponseDTO(10L, "Paycheck", "2026-01-15", 1, 1500.0,
                "income", "fixed", new CategoryResponseDTO(5L, "Salary", "#00FF00"));
    }

    @Test
    void createTransaction_withValidBody_returnsCreatedTransaction() throws Exception {
        when(transactionService.createTransaction(any(TransactionRequestDTO.class), any(User.class)))
                .thenReturn(responseDTO());

        mockMvc.perform(post("/transaction").with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Paycheck",
                                  "transactionDate": "2026-01-15",
                                  "amount": 1,
                                  "price": 1500.0,
                                  "transactionType": "income",
                                  "transactionSubtype": "fixed",
                                  "category": {"id": 5, "name": "Salary", "color": "#00FF00"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Paycheck"))
                .andExpect(jsonPath("$.transactionDate").value("2026-01-15"))
                .andExpect(jsonPath("$.amount").value(1))
                .andExpect(jsonPath("$.price").value(1500.0))
                .andExpect(jsonPath("$.transactionType").value("income"))
                .andExpect(jsonPath("$.transactionSubtype").value("fixed"))
                .andExpect(jsonPath("$.category.id").value(5));
    }

    @Test
    void createTransaction_withUnknownCategory_returns404WithMessage() throws Exception {
        when(transactionService.createTransaction(any(TransactionRequestDTO.class), any(User.class)))
                .thenThrow(new NotFoundException("category not found"));

        mockMvc.perform(post("/transaction").with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Paycheck",
                                  "transactionDate": "2026-01-15",
                                  "amount": 1,
                                  "price": 1500.0,
                                  "transactionType": "income",
                                  "transactionSubtype": "fixed",
                                  "category": {"id": 999, "name": "Salary", "color": "#00FF00"}
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("category not found"));
    }

    @Test
    void createTransaction_withBlankName_returns400ValidationMessage() throws Exception {
        mockMvc.perform(post("/transaction").with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "transactionDate": "2026-01-15",
                                  "amount": 1,
                                  "price": 1500.0,
                                  "transactionType": "income",
                                  "transactionSubtype": "fixed",
                                  "category": {"id": 5, "name": "Salary", "color": "#00FF00"}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("invalid body:")));

        verify(transactionService, never()).createTransaction(any(), any());
    }

    @Test
    void getAllTransactions_returnsPagedContent() throws Exception {
        when(transactionService.getAllTransaction(eq(principal), any(TransactionFilter.class), any(Pageable.class)))
                .thenReturn(Page.of(List.of(responseDTO()), 0, 10, 1, 1));

        mockMvc.perform(get("/transaction/all").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].name").value("Paycheck"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAllTransactions_forwardsTypeAndDateFiltersToService() throws Exception {
        when(transactionService.getAllTransaction(eq(principal), any(TransactionFilter.class), any(Pageable.class)))
                .thenReturn(Page.of(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/transaction/all").with(authentication(auth()))
                        .param("type", "expense")
                        .param("subType", "variable")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk());

        var filterCaptor = org.mockito.ArgumentCaptor.forClass(TransactionFilter.class);
        verify(transactionService).getAllTransaction(eq(principal), filterCaptor.capture(), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(filterCaptor.getValue().type().getName()).isEqualTo("expense");
        org.assertj.core.api.Assertions.assertThat(filterCaptor.getValue().subtype().getName()).isEqualTo("variable");
        org.assertj.core.api.Assertions.assertThat(filterCaptor.getValue().from()).isEqualTo(LocalDate.of(2026, 1, 1));
        org.assertj.core.api.Assertions.assertThat(filterCaptor.getValue().to()).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    void getTransaction_withExistingId_returnsTransaction() throws Exception {
        when(transactionService.getTransaction(10L, principal)).thenReturn(responseDTO());

        mockMvc.perform(get("/transaction/10").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Paycheck"));
    }

    @Test
    void getTransaction_ofAnotherUser_returns404() throws Exception {
        when(transactionService.getTransaction(99L, principal)).thenThrow(new NotFoundException("transaction not found"));

        mockMvc.perform(get("/transaction/99").with(authentication(auth())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("transaction not found"));
    }

    @Test
    void updateTransaction_returnsUpdatedTransaction() throws Exception {
        when(transactionService.updateTransaction(any(TransactionRequestDTO.class), eq(10L), eq(principal)))
                .thenReturn(new TransactionResponseDTO(10L, "Groceries", "2026-02-20", 3, 45.5,
                        "expense", "variable", new CategoryResponseDTO(7L, "Food", "#FF0000")));

        mockMvc.perform(put("/transaction/10").with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Groceries",
                                  "transactionDate": "2026-02-20",
                                  "amount": 3,
                                  "price": 45.5,
                                  "transactionType": "expense",
                                  "transactionSubtype": "variable",
                                  "category": {"id": 7, "name": "Food", "color": "#FF0000"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Groceries"))
                .andExpect(jsonPath("$.transactionType").value("expense"));
    }

    @Test
    void deleteTransaction_returnsConfirmationMessage() throws Exception {
        when(transactionService.deleteTransaction(10L, principal)).thenReturn("transaction delete");

        mockMvc.perform(delete("/transaction/10").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("transaction delete"));

        verify(transactionService).deleteTransaction(10L, principal);
    }

    @Test
    void getTransaction_withNonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/transaction/not-a-number").with(authentication(auth())))
                .andExpect(status().isBadRequest());
    }
}

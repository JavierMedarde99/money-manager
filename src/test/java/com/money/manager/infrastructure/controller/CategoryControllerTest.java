package com.money.manager.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

import com.money.manager.application.dtos.CategoryResponseDTO;
import com.money.manager.application.ports.CategoryService;
import com.money.manager.domain.User;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.domain.paging.SortDirection;
import com.money.manager.infrastructure.config.JwtFilter;
import com.money.manager.infrastructure.security.RateLimiterFilter;

@WebMvcTest(controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtFilter.class, RateLimiterFilter.class }))
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

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

    private CategoryResponseDTO responseDTO() {
        return new CategoryResponseDTO(5L, "Salary", "#00FF00");
    }

    @Test
    void getAllCategories_withDefaults_returnsPagedContent() throws Exception {
        when(categoryService.getCategoryByUser(eq(principal), any(Pageable.class)))
                .thenReturn(Page.of(List.of(responseDTO()), 0, 10, 1, 1));

        mockMvc.perform(get("/category/all").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].name").value("Salary"))
                .andExpect(jsonPath("$.content[0].color").value("#00FF00"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAllCategories_forwardsDefaultPageableToService() throws Exception {
        when(categoryService.getCategoryByUser(eq(principal), any(Pageable.class)))
                .thenReturn(Page.of(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/category/all").with(authentication(auth())))
                .andExpect(status().isOk());

        var pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(categoryService).getCategoryByUser(eq(principal), pageableCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().page()).isZero();
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().size()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().sortBy()).isEqualTo("id");
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().direction())
                .isEqualTo(SortDirection.DESC);
    }

    @Test
    void getAllCategories_forwardsSortParamsToPageable() throws Exception {
        when(categoryService.getCategoryByUser(eq(principal), any(Pageable.class)))
                .thenReturn(Page.of(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/category/all").with(authentication(auth()))
                        .param("page", "2")
                        .param("size", "25")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk());

        var pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(categoryService).getCategoryByUser(eq(principal), pageableCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().page()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().size()).isEqualTo(25);
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().sortBy()).isEqualTo("name");
        org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().direction())
                .isEqualTo(SortDirection.ASC);
    }
}
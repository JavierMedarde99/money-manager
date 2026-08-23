package com.money.manager.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.money.manager.application.ports.TokenService;
import com.money.manager.application.ports.UserService;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(tokenService, userService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void withoutAuthorizationHeader_continuesChainWithoutAuthentication() throws Exception {
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenService, userService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void withNonBearerHeader_continuesChainWithoutAuthentication() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenService, userService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void withValidToken_setsAuthenticationWithUserPrincipal() throws Exception {
        User user = User.builder().id(1L).username("javi").build();
        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenService.getUserFromToken("valid-token")).thenReturn("javi");
        when(userService.getUser("javi")).thenReturn(user);
        when(tokenService.validateToken("valid-token")).thenReturn(true);

        jwtFilter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        verify(filterChain).doFilter(request, response);
        assertThat(authentication).isNotNull().isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isSameAs(user);
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.isAuthenticated()).isTrue();
    }

    @Test
    void withInvalidJwt_clearsContextButContinuesChain() throws Exception {
        request.addHeader("Authorization", "Bearer broken-token");
        when(tokenService.getUserFromToken("broken-token")).thenThrow(new JwtException("malformed token"));

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void withUnknownUser_clearsContextButContinuesChain() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenService.getUserFromToken("valid-token")).thenReturn("ghost");
        when(userService.getUser("ghost")).thenThrow(new NotFoundException("User not found"));

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void withInvalidTokenSignature_doesNotSetAuthentication() throws Exception {
        User user = User.builder().id(1L).username("javi").build();
        request.addHeader("Authorization", "Bearer expired-token");
        when(tokenService.getUserFromToken("expired-token")).thenReturn("javi");
        when(userService.getUser("javi")).thenReturn(user);
        when(tokenService.validateToken("expired-token")).thenReturn(false);

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void withMismatchedUsername_doesNotSetAuthentication() throws Exception {
        User user = User.builder().id(1L).username("other-user").build();
        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenService.getUserFromToken("valid-token")).thenReturn("javi");
        when(userService.getUser("javi")).thenReturn(user);
        when(tokenService.validateToken("valid-token")).thenReturn(true);

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void withExistingAuthentication_doesNotOverrideIt() throws Exception {
        User existingPrincipal = User.builder().id(2L).username("existing").build();
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(existingPrincipal, null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenService.getUserFromToken("valid-token")).thenReturn("javi");

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isSameAs(existingPrincipal);
        verify(userService, never()).getUser("javi");
        verify(tokenService, never()).validateToken("valid-token");
    }
}

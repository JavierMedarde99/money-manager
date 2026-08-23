package com.money.manager.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class RateLimiterFilterTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private FilterChain filterChain;

    private RateLimiterFilter rateLimiterFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final long WINDOW_MILLIS = 60_000L;

    @BeforeEach
    void setUp() {
        rateLimiterFilter = new RateLimiterFilter(rateLimiterService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("User-Agent", "test-agent");
    }

    @Test
    void nonLimitedEndpoint_passesThroughWithoutRateLimiting() throws ServletException, IOException {
        request.setRequestURI("/health");
        request.setMethod("GET");

        rateLimiterFilter.doFilter(request, response, filterChain);

        verifyNoInteractions(rateLimiterService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void nonPostRequestToLogin_passesThrough() throws ServletException, IOException {
        request.setRequestURI("/user/login");
        request.setMethod("GET");

        rateLimiterFilter.doFilter(request, response, filterChain);

        verifyNoInteractions(rateLimiterService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void loginWithinLimit_proceedsToChain() throws ServletException, IOException {
        request.setRequestURI("/user/login");
        request.setMethod("POST");
        when(rateLimiterService.isAllowed(contains("login:"), eq(5), eq(WINDOW_MILLIS))).thenReturn(true);

        rateLimiterFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void loginWhenExceeded_returns429AndBlocksChain() throws ServletException, IOException {
        request.setRequestURI("/user/login");
        request.setMethod("POST");
        when(rateLimiterService.isAllowed(anyString(), eq(5), eq(WINDOW_MILLIS))).thenReturn(false);

        rateLimiterFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"status\":429").contains("too many requests");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void registerWhenExceeded_returns429WithRegisterKeyAndLimit() throws ServletException, IOException {
        request.setRequestURI("/user");
        request.setMethod("POST");
        when(rateLimiterService.isAllowed(anyString(), eq(10), eq(WINDOW_MILLIS))).thenReturn(false);

        rateLimiterFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        verify(filterChain, never()).doFilter(request, response);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rateLimiterService).isAllowed(keyCaptor.capture(), eq(10), eq(WINDOW_MILLIS));
        assertThat(keyCaptor.getValue())
                .startsWith("register:")
                .contains("10.0.0.1")
                .contains("test-agent");
    }

    @Test
    void loginUsesClientAddressAndUserAgentInKey() throws ServletException, IOException {
        request.setRequestURI("/user/login");
        request.setMethod("POST");
        when(rateLimiterService.isAllowed(anyString(), anyInt(), anyLong())).thenReturn(true);

        rateLimiterFilter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rateLimiterService).isAllowed(keyCaptor.capture(), eq(5), eq(WINDOW_MILLIS));
        assertThat(keyCaptor.getValue())
                .startsWith("login:")
                .contains("10.0.0.1")
                .contains("test-agent");
    }
}

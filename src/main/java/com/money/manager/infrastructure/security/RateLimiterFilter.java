package com.money.manager.infrastructure.security;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_LOGIN_REQUESTS = 5;
    private static final int MAX_REGISTER_REQUESTS = 10;

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // You just need to apply rate limits to the endpoints that don't require authorization.
        boolean isLogin = request.getRequestURI().equals("/user/login")
                && HttpMethod.POST.matches(request.getMethod());
        boolean isRegister = request.getRequestURI().equals("/user")
                && HttpMethod.POST.matches(request.getMethod());

        if (!isLogin && !isRegister) {
            filterChain.doFilter(request, response);
            return;
        }

        // key is remote access to identify the user making the call.
        String key = (isLogin ? "login:" : "register:") + request.getRemoteAddr();
        int maxRequests = isLogin ? MAX_LOGIN_REQUESTS : MAX_REGISTER_REQUESTS;

        if (!rateLimiterService.isAllowed(key, maxRequests, WINDOW_MILLIS)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {"status":429,"message":"too many requests, try again later"}
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}

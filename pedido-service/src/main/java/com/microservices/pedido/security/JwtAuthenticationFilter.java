package com.microservices.pedido.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenValidator jwtTokenValidator;

    @Value("${jwt.enabled:true}")
    private boolean jwtEnabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            reject(response, "Token requerido");
            return;
        }

        try {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            Claims claims = jwtTokenValidator.validate(token);
            request.setAttribute("auth.username", claims.getSubject());
            request.setAttribute("auth.userId", claims.get("userId"));
            request.setAttribute("auth.roles", claims.get("roles"));
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.warn("Token JWT rechazado: {}", ex.getMessage());
            reject(response, "Token invalido");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!jwtEnabled) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/actuator/health");
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

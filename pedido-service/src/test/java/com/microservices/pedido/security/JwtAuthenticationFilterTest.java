package com.microservices.pedido.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "microservicios-secret-key-para-firmar-jwt-en-desarrollo-2026";

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(new JwtTokenValidator(SECRET));
        ReflectionTestUtils.setField(filter, "jwtEnabled", true);
    }

    @Test
    @DisplayName("Rechaza request sin token")
    void rejectsRequestWithoutToken() throws ServletException, IOException {
        MockHttpServletResponse response = executeWithAuthorization(null);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token requerido");
    }

    @Test
    @DisplayName("Rechaza request con token invalido")
    void rejectsRequestWithInvalidToken() throws ServletException, IOException {
        MockHttpServletResponse response = executeWithAuthorization("Bearer token-invalido");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token invalido");
    }

    @Test
    @DisplayName("Permite request con token valido y claims requeridos")
    void acceptsRequestWithValidToken() throws ServletException, IOException {
        MockHttpServletRequest request = protectedRequest();
        request.addHeader("Authorization", "Bearer " + validToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(request.getAttribute("auth.username")).isEqualTo("juan");
        assertThat(request.getAttribute("auth.userId")).isEqualTo(1);
        assertThat(request.getAttribute("auth.roles")).isEqualTo(List.of("USER"));
    }

    private MockHttpServletResponse executeWithAuthorization(String authorizationHeader)
            throws ServletException, IOException {
        MockHttpServletRequest request = protectedRequest();
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        return response;
    }

    private MockHttpServletRequest protectedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pedidos");
        request.setServletPath("/api/v1/pedidos");
        return request;
    }

    private String validToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject("juan")
                .claim("userId", 1L)
                .claim("roles", List.of("USER"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}

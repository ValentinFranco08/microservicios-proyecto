package com.microservices.auth.controller;

import com.microservices.auth.dto.JwtPayload;
import com.microservices.auth.service.AuthService;
import com.microservices.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private JwtService jwtService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        AuthController controller = new AuthController(mock(AuthService.class), jwtService);
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    void profileDevuelveLosDatosExtraidosDelToken() throws Exception {
        when(jwtService.verifyToken("token-valido")).thenReturn(
                new JwtPayload(1L, "test@mail.com", "USER", Instant.now().plusSeconds(3600)));

        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void profileRechazaUnaPeticionSinToken() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(jwtService);
    }

    @Test
    void profileRechazaUnTokenInvalido() throws Exception {
        when(jwtService.verifyToken("token-invalido")).thenThrow(new JwtException("firma invalida"));

        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token invalido o expirado"));
    }
}

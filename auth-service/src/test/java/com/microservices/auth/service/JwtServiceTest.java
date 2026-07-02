package com.microservices.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.dto.JwtPayload;
import com.microservices.auth.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "clave-secreta-de-pruebas-con-al-menos-32-bytes";
    private static final Instant NOW = Instant.parse("2026-07-02T12:00:00Z");
    private static final User USER = new User(1L, "test@mail.com", "USER");

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = serviceWith(SECRET, NOW);
    }

    @Test
    void generaUnTokenConLosDatosDelUsuarioYExpiracion() {
        String token = jwtService.generateToken(USER);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);

        JwtPayload payload = jwtService.verifyToken(token);
        assertEquals(USER.email(), payload.email());
        assertEquals(USER.id(), payload.id());
        assertEquals(USER.role(), payload.role());
        assertEquals(NOW.plusSeconds(3600), payload.expiration());
    }

    @Test
    void verificaUnTokenValido() {
        JwtPayload payload = jwtService.verifyToken(jwtService.generateToken(USER));

        assertEquals(USER.id(), payload.id());
        assertEquals(USER.email(), payload.email());
    }

    @Test
    void rechazaUnTokenModificado() {
        String token = jwtService.generateToken(USER);
        String[] parts = token.split("\\.");
        String changedPayload = (parts[1].startsWith("A") ? "B" : "A") + parts[1].substring(1);

        assertThrows(JwtException.class,
                () -> jwtService.verifyToken(parts[0] + "." + changedPayload + "." + parts[2]));
    }

    @Test
    void rechazaUnTokenVencido() {
        String token = jwtService.generateToken(USER);
        JwtService futureService = serviceWith(SECRET, NOW.plusSeconds(3601));

        assertThrows(ExpiredJwtException.class, () -> futureService.verifyToken(token));
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        JwtService otherService = serviceWith(
                "una-clave-totalmente-distinta-y-tambien-segura-123", NOW);
        String token = otherService.generateToken(USER);

        assertThrows(JwtException.class, () -> jwtService.verifyToken(token));
    }

    @Test
    void decodificaTokenSinVerificarSuFirma() {
        JwtPayload payload = jwtService.decodeToken(jwtService.generateToken(USER));

        assertNotNull(payload);
        assertEquals(USER.id(), payload.id());
        assertEquals(USER.email(), payload.email());
        assertEquals(USER.role(), payload.role());
        assertEquals(NOW.plusSeconds(3600), payload.expiration());
    }

    @Test
    void devuelveNullAlDecodificarUnTokenInvalido() {
        assertNull(jwtService.decodeToken("esto-no-es-un-jwt"));
        assertNull(jwtService.decodeToken(null));
    }

    private JwtService serviceWith(String secret, Instant instant) {
        return new JwtService(
                secret,
                3_600_000,
                Clock.fixed(instant, ZoneOffset.UTC),
                new ObjectMapper());
    }
}

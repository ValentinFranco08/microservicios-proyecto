package com.microservices.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.dto.JwtPayload;
import com.microservices.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationMs;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs,
            ObjectMapper objectMapper) {
        this(secret, expirationMs, Clock.systemUTC(), objectMapper);
    }

    JwtService(String secret, long expirationMs, Clock clock, ObjectMapper objectMapper) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    public String generateToken(User user) {
        Instant issuedAt = clock.instant();
        Instant expiration = issuedAt.plusMillis(expirationMs);

        return Jwts.builder()
                .claim("id", user.id())
                .claim("email", user.email())
                .claim("role", user.role())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtPayload verifyToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .setClock(() -> Date.from(clock.instant()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return toPayload(claims);
    }

    public JwtPayload decodeToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode claims = objectMapper.readTree(decodedPayload);
            if (!claims.hasNonNull("id") || !claims.hasNonNull("email")
                    || !claims.hasNonNull("role") || !claims.hasNonNull("exp")) {
                return null;
            }

            return new JwtPayload(
                    claims.get("id").longValue(),
                    claims.get("email").asText(),
                    claims.get("role").asText(),
                    Instant.ofEpochSecond(claims.get("exp").longValue()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private JwtPayload toPayload(Claims claims) {
        Number id = claims.get("id", Number.class);
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        if (id == null || email == null || role == null || claims.getExpiration() == null) {
            throw new MalformedJwtException("El token no contiene los claims requeridos");
        }
        return new JwtPayload(
                id.longValue(),
                email,
                role,
                claims.getExpiration().toInstant());
    }
}

package com.microservices.pedido.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtTokenValidator {

    private final Key signingKey;

    public JwtTokenValidator(@Value("${jwt.secret}") String jwtSecret) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new IllegalArgumentException("El token no contiene subject");
        }
        if (claims.get("userId") == null) {
            throw new IllegalArgumentException("El token no contiene userId");
        }
        if (claims.get("roles") == null) {
            throw new IllegalArgumentException("El token no contiene roles");
        }

        return claims;
    }
}

package com.microservices.auth.service;

import com.microservices.auth.dto.AuthResponse;
import com.microservices.auth.dto.CreateUserRequest;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.UserResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthService {

    private final Map<String, AuthUser> users = new ConcurrentHashMap<>();
    private final AtomicLong userIdSequence = new AtomicLong(1);
    private final PasswordEncoder passwordEncoder;
    private final Key signingKey;
    private final long expirationMs;

    public AuthService(
            PasswordEncoder passwordEncoder,
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.passwordEncoder = passwordEncoder;
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public UserResponse createUser(CreateUserRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());

        String username = request.getUsername().trim();
        AuthUser user = new AuthUser(
                userIdSequence.getAndIncrement(),
                username,
                passwordEncoder.encode(request.getPassword()),
                List.of("USER"));

        AuthUser previousUser = users.putIfAbsent(username, user);
        if (previousUser != null) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        return new UserResponse(user.id(), user.username());
    }

    public AuthResponse login(LoginRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());

        String username = request.getUsername().trim();
        AuthUser user = users.get(username);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.passwordHash())) {
            throw new IllegalArgumentException("Usuario o contrasena invalida");
        }

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);
        String token = Jwts.builder()
                .setSubject(user.username())
                .claim("userId", user.id())
                .claim("roles", user.roles())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        return new AuthResponse(token, "Bearer", expirationMs);
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El username es obligatorio");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }
    }

    private record AuthUser(Long id, String username, String passwordHash, List<String> roles) {
    }
}

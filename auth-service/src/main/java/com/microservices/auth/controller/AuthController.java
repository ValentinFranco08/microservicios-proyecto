package com.microservices.auth.controller;

import com.microservices.auth.dto.AuthResponse;
import com.microservices.auth.dto.CreateUserRequest;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.JwtPayload;
import com.microservices.auth.dto.ProfileResponse;
import com.microservices.auth.dto.UserResponse;
import com.microservices.auth.service.AuthService;
import com.microservices.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacion y emision de tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/create-user")
    @Operation(summary = "Crear usuario", description = "Crea un usuario en memoria para autenticarse")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Valida credenciales y devuelve un token JWT")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/api/auth/profile")
    @Operation(summary = "Ver perfil", description = "Devuelve los datos del token JWT verificado")
    public ResponseEntity<ProfileResponse> profile(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JwtPayload payload = jwtService.verifyToken(authorization.substring(7));
        return ResponseEntity.ok(new ProfileResponse(payload.id(), payload.email(), payload.role()));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<String> handleJwtException(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalido o expirado");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

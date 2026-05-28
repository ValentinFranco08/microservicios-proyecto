package com.microservices.auth.controller;

import com.microservices.auth.dto.AuthResponse;
import com.microservices.auth.dto.CreateUserRequest;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.UserResponse;
import com.microservices.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacion y emision de tokens JWT")
public class AuthController {

    private final AuthService authService;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

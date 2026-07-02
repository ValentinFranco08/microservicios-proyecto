package com.microservices.auth.dto;

import java.time.Instant;

public record JwtPayload(Long id, String email, String role, Instant expiration) {
}

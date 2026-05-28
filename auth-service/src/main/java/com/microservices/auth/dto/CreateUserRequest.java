package com.microservices.auth.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
}

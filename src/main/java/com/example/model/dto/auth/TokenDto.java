package com.example.model.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TokenDto {
    private String token;
    private Long expiry;          // Access token expiry
    private Long refreshExpiry;   // Refresh token expiry - ADD THIS!
}
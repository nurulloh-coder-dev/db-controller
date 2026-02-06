package com.example.model.dto.auth;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private long expiry;
    private String refreshToken;
    private long refreshExpiry;
}

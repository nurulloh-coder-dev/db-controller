package com.example.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PasswordGenerator {
    public String generatePassword() {
        String uuidPassword = UUID.randomUUID().toString();
        return uuidPassword.substring(0,7).replace("-", "");
    }
}

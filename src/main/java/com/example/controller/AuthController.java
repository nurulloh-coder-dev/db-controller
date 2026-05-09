package com.example.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.aop.SkipActivityTracking;
import com.example.model.dto.auth.LoginRequest;
import com.example.model.dto.auth.LoginResponse;
import com.example.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SkipActivityTracking
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, @RequestParam(defaultValue = "en") String lang) {
        try {
            LoginResponse response = service.login(request.getUsername(), request.getPassword(),lang);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody Map<String, String> body,
            @RequestParam(defaultValue = "en") String lang
    ){
        String message = service.forgotPassword(body.get("email"), lang);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody Map<String, String> body,
            @RequestParam(defaultValue = "en") String lang
    ){
        String message = service.resetPassword(body.get("token"), body.get("newPassword"), lang);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = service.refreshToken(refreshToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
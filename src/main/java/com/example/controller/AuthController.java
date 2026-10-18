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
        LoginResponse response = service.login(request.getUsername(), request.getPassword(), lang);
        return new ResponseEntity<>(response, HttpStatus.OK);
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

    /**
     * The refresh token belongs in the body; the query parameter is still read so pages loaded
     * before this release keep working, but URLs end up in logs, so it should go away.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody(required = false) Map<String, String> body,
                                                      @RequestParam(required = false) String refreshToken) {
        String token = body != null && body.get("refreshToken") != null ? body.get("refreshToken") : refreshToken;
        LoginResponse response = service.refreshToken(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) Map<String, String> body) {
        service.logout(body == null ? null : body.get("refreshToken"));
        return ResponseEntity.noContent().build();
    }
}

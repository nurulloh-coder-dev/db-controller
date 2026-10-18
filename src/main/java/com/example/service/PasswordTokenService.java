package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * One-time links for setting or resetting a password. Only a hash of the token is stored (in Redis),
 * so a leaked Redis snapshot can't be used to reset passwords.
 */
@Service
@RequiredArgsConstructor
public class PasswordTokenService {
    private static final String KEY_PREFIX = "pwd-token:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedisTemplate<String, String> redis;

    @Value("${app.frontend-url:https://db-controller.xyz}")
    private String frontendUrl;

    /** Stores a new token for this email and returns the link to put in the email. */
    public String createLink(String email, Duration validFor) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(KEY_PREFIX + hash(rawToken), email, validFor);
        return frontendUrl + "/login/reset-password.html?token=" + rawToken;
    }

    /** Returns the email the token was issued for and invalidates it (one-time use). */
    public Optional<String> consume(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        String key = KEY_PREFIX + hash(rawToken);
        String email = redis.opsForValue().get(key);
        if (email != null) {
            redis.delete(key);
        }
        return Optional.ofNullable(email);
    }

    @SneakyThrows
    private static String hash(String rawToken) {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}

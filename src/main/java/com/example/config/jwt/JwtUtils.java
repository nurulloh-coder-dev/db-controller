package com.example.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.model.entity.AuthUser;
import com.example.model.dto.auth.TokenDto;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
public class JwtUtils {
    public static final String TYPE_CLAIM = "type";
    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";

    @Value("${app-security.token.access-exp:900}")
    private Long accessTokenExpiration;

    @Value("${app-security.token.refresh-exp:86400}")
    private Long refreshTokenExpiration;

    // no default: a secret written in the source would let anyone sign tokens
    @Value("${app-security.token.secret:}")
    private String secretKey;

    @PostConstruct
    void checkSecret() {
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("SECRET_KEY must be set to at least 32 bytes");
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public TokenDto generateAccessToken(String subject, Map<String, Object> claims) {
        long expiryTimestamp = System.currentTimeMillis() + (accessTokenExpiration * 1000);
        return TokenDto.builder()
                .expiry(expiryTimestamp)
                .token(buildToken(subject, claims, ACCESS, expiryTimestamp))
                .build();
    }

    public TokenDto generateRefreshToken(String subject, Map<String, Object> claims) {
        long expiryTimestamp = System.currentTimeMillis() + (refreshTokenExpiration * 1000);
        return TokenDto.builder()
                .token(buildToken(subject, claims, REFRESH, expiryTimestamp))
                .refreshExpiry(expiryTimestamp)
                .build();
    }

    private String buildToken(String subject, Map<String, Object> claims, String type, long expiryTimestamp) {
        Map<String, Object> allClaims = new HashMap<>();
        if (claims != null) {
            allClaims.putAll(claims);
        }
        allClaims.put(TYPE_CLAIM, type);
        return Jwts.builder()
                .claims(allClaims)
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(expiryTimestamp))
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, Object> prepareClaims(AuthUser authUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", authUser.getId());
        return claims;
    }

    public boolean isTokenValid(String token, Claims claims) {
        try {
            String username = claims.getSubject();
            return username != null && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isType(Claims claims, String type) {
        return type.equals(claims.get(TYPE_CLAIM, String.class));
    }

    /**
     * Tokens issued before the user's last password change are no longer accepted.
     * Compared in whole seconds because JWT timestamps have second precision.
     */
    public boolean issuedBeforePasswordChange(Claims claims, AuthUser authUser) {
        Instant changedAt = authUser.getPasswordChangedAt();
        Date issuedAt = claims.getIssuedAt();
        return changedAt != null && (issuedAt == null || issuedAt.toInstant().getEpochSecond() < changedAt.getEpochSecond());
    }
}

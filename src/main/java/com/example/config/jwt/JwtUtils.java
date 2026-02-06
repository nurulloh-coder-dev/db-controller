package com.example.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.model.entity.AuthUser;
import com.example.model.dto.auth.TokenDto;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtUtils {

    @Value("${app-security.token.access-exp:180}")
    private Long accessTokenExpiration;

    @Value("${app-security.token.refresh-exp:86400}")
    private Long refreshTokenExpiration;

    @Value("${app-security.token.secret:bu_secret_key_uzunligi_kamida_32_byte_bolishi_zarur}")
    private String secretKey;

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public TokenDto generateAccessToken(String subject, Map<String, Object> claims) {
        long expiryTimestamp = System.currentTimeMillis() + (accessTokenExpiration * 1000);
        Date exp = new Date(expiryTimestamp);

        String token = Jwts.builder()
                .subject(subject)
                .expiration(exp)
                .signWith(getSecretKey())
                .issuedAt(new Date())
                .claims(claims)
                .compact();

        return TokenDto.builder()
                .expiry(expiryTimestamp)  // ← Return as Long (milliseconds)
                .token(token)
                .build();
    }

    public TokenDto generateRefreshToken(String subject, Map<String, Object> claims) {
        long expiryTimestamp = System.currentTimeMillis() + (refreshTokenExpiration * 1000);
        Date exp = new Date(expiryTimestamp);
        String token = Jwts.builder()
                .subject(subject)
                .expiration(exp)
                .signWith(getSecretKey())
                .issuedAt(new Date())
                .claims(claims)
                .compact();

        return TokenDto.builder()
                .token(token)
                .refreshExpiry(expiryTimestamp)  // ✅ FIXED
                .build();
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


    // springdoc

}

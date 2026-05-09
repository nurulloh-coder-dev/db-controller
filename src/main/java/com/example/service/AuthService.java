package com.example.service;

import com.example.util.Utils;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.config.jwt.JwtUtils;
import com.example.controller.active_users.UserSession;
import com.example.controller.active_users.UserSessionRepository;
import com.example.controller.active_users.UserSessionRedisService;
import com.example.model.entity.AuthUser;
import com.example.model.dto.authUser.AuthUserDto;
import com.example.model.dto.auth.LoginResponse;
import com.example.model.dto.auth.TokenDto;
import com.example.repository.AuthUserRepository;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class AuthService {
    private final RedisTemplate<String, String> redis;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserRepository repository;
    private final JwtUtils jwtUtils;
    private final AuthUserRepository authUserRepository;
    private final UserSessionRedisService userSessionService;
    private final UserSessionRepository userSessionRepository;
    private final MessageSource messageSource;
    private final Utils utils;
    private final EmailService emailService;


    public LoginResponse login(String username, String password, String lang) {

        AuthUser authUser = authUserRepository.findByUsernameAndDeletedFalse(username).orElseThrow(
                () -> new BadCredentialsException(messageSource.getMessage("username.password.error", null, utils.getLocaleByLanguage(lang)))
        );

        if (!passwordEncoder.matches(password, authUser.getPassword())) {
            throw new BadCredentialsException(messageSource.getMessage("username.password.error", null, utils.getLocaleByLanguage(lang)));
        }

        Map<String, Object> stringObjectMap = jwtUtils.prepareClaims(authUser);
        TokenDto accessToken = jwtUtils.generateAccessToken(username, stringObjectMap);
        TokenDto refreshToken = jwtUtils.generateRefreshToken(username, null);
        if (userSessionService.isRedisRunning()) {
            UserSession userSession = new UserSession();
            userSession.setUserId(authUser.getId());
            userSession.setLoginTime(Instant.now());
            userSession.setLastActivityTime(Instant.now());
            userSessionRepository.save(userSession);
            userSessionService.markActive(authUser.getId());
        }
        return LoginResponse.builder()
                .token(accessToken.getToken())
                .expiry(accessToken.getExpiry())
                .refreshToken(refreshToken.getToken())
                .refreshExpiry(refreshToken.getRefreshExpiry())
                .build();
    }

    public List<AuthUserDto> getAll() {
        List<AuthUser> users = repository.findAll();

        return users.stream().map(u -> AuthUserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .build()).toList();
    }

    public LoginResponse refreshToken(String refreshToken) {
        Claims claims = jwtUtils.extractClaims(refreshToken);
        String username = claims.getSubject();
        AuthUser authUser = authUserRepository.findByUsernameAndDeletedFalse(username).orElseThrow(
                () -> new BadCredentialsException("Invalid refresh token")
        );
        TokenDto access = jwtUtils.generateAccessToken(username, jwtUtils.prepareClaims(authUser));
        TokenDto refresh = jwtUtils.generateRefreshToken(username, null);
        return LoginResponse.builder()
                .token(access.getToken())
                .expiry(access.getExpiry())
                .refreshToken(refresh.getToken())
                .refreshExpiry(refresh.getRefreshExpiry())
                .build();
    }


    public String forgotPassword(String email, String lang) {
        String msg = messageSource.getMessage("password.link", null, utils.getLocaleByLanguage(lang));

        boolean present = repository.checkEmail(email).isPresent();
        if (!present) {
            return msg;
        }

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        emailService.sendResetLink(email, rawToken);
        redis.opsForValue().set(tokenHash, email, 15, TimeUnit.MINUTES);
        return msg;
    }
    public String resetPassword(String rawToken, String newPassword, String lang) {
        String tokenHash = hashToken(rawToken);

        String email = redis.opsForValue().get(tokenHash);
        if (email == null) {
            throw new RuntimeException("Invalid or expired token");
        }

        AuthUser user = repository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);

        redis.delete(tokenHash); // one time use, delete after reset

        return messageSource.getMessage("password.reset.success", null, utils.getLocaleByLanguage(lang));
    }

    @SneakyThrows
    private String hashToken(String rawToken) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

        // convert bytes to hex string
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}

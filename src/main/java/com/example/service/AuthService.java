package com.example.service;

import com.example.exception.InvalidInput;
import com.example.util.Utils;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class AuthService {
    private static final String REVOKED_PREFIX = "revoked-refresh:";
    static final int MIN_PASSWORD_LENGTH = 8;

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
    private final PasswordTokenService passwordTokenService;
    private final LoginAttemptService loginAttemptService;


    public LoginResponse login(String username, String password, String lang) {
        if (loginAttemptService.isLocked(username)) {
            throw new BadCredentialsException("Too many failed attempts. Try again in 15 minutes.");
        }

        AuthUser authUser = authUserRepository.findByUsernameAndDeletedFalse(username).orElse(null);
        if (authUser == null || !passwordEncoder.matches(password, authUser.getPassword())) {
            loginAttemptService.recordFailure(username);
            throw new BadCredentialsException(messageSource.getMessage("username.password.error", null, utils.getLocaleByLanguage(lang)));
        }
        loginAttemptService.recordSuccess(username);

        if (userSessionService.isRedisRunning()) {
            UserSession userSession = new UserSession();
            userSession.setUserId(authUser.getId());
            userSession.setLoginTime(Instant.now());
            userSession.setLastActivityTime(Instant.now());
            userSessionRepository.save(userSession);
            userSessionService.markActive(authUser.getId());
        }
        return issueTokens(authUser);
    }

    public List<AuthUserDto> getAll() {
        List<AuthUser> users = repository.findAll();

        return users.stream().map(u -> AuthUserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .build()).toList();
    }

    /**
     * Exchanges a refresh token for a new token pair. Refresh tokens are single use: the old one is
     * revoked, so a stolen copy stops working as soon as either party uses it.
     */
    public LoginResponse refreshToken(String refreshToken) {
        Claims claims = parseRefreshToken(refreshToken);
        if (isRevoked(claims)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        AuthUser authUser = authUserRepository.findByUsernameAndDeletedFalse(claims.getSubject()).orElseThrow(
                () -> new BadCredentialsException("Invalid refresh token")
        );
        if (jwtUtils.issuedBeforePasswordChange(claims, authUser)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        revoke(claims);
        return issueTokens(authUser);
    }

    /** Revokes the given refresh token; the short-lived access token simply expires. */
    public void logout(String refreshToken) {
        try {
            revoke(parseRefreshToken(refreshToken));
        } catch (Exception e) {
            // already invalid: nothing to revoke
        }
    }

    public String forgotPassword(String email, String lang) {
        String msg = messageSource.getMessage("password.link", null, utils.getLocaleByLanguage(lang));
        if (email == null || repository.checkEmail(email).isEmpty()) {
            return msg; // same answer either way, so emails can't be probed
        }
        emailService.sendResetLink(email, passwordTokenService.createLink(email, Duration.ofMinutes(15)));
        return msg;
    }

    public String resetPassword(String rawToken, String newPassword, String lang) {
        checkNewPassword(newPassword);
        String email = passwordTokenService.consume(rawToken)
                .orElseThrow(() -> new InvalidInput("Invalid or expired link"));

        AuthUser user = repository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new InvalidInput("Invalid or expired link"));

        setPassword(user, newPassword);
        return messageSource.getMessage("password.reset.success", null, utils.getLocaleByLanguage(lang));
    }

    /** Sets a new login password and signs out every session issued before it. */
    public void setPassword(AuthUser user, String newPassword) {
        checkNewPassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        repository.save(user);
    }

    static void checkNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInput("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    private LoginResponse issueTokens(AuthUser authUser) {
        Map<String, Object> claims = jwtUtils.prepareClaims(authUser);
        TokenDto access = jwtUtils.generateAccessToken(authUser.getUsername(), claims);
        TokenDto refresh = jwtUtils.generateRefreshToken(authUser.getUsername(), null);
        return LoginResponse.builder()
                .token(access.getToken())
                .expiry(access.getExpiry())
                .refreshToken(refresh.getToken())
                .refreshExpiry(refresh.getRefreshExpiry())
                .build();
    }

    private Claims parseRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        Claims claims;
        try {
            claims = jwtUtils.extractClaims(refreshToken);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        if (!jwtUtils.isType(claims, JwtUtils.REFRESH) || claims.getId() == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        return claims;
    }

    private boolean isRevoked(Claims claims) {
        try {
            return Boolean.TRUE.equals(redis.hasKey(REVOKED_PREFIX + claims.getId()));
        } catch (Exception e) {
            log.warn("Redis unavailable, refresh token revocation not checked: {}", e.getMessage());
            return false;
        }
    }

    private void revoke(Claims claims) {
        Duration remaining = Duration.between(Instant.now(), claims.getExpiration().toInstant());
        if (remaining.isNegative() || remaining.isZero()) {
            return;
        }
        try {
            redis.opsForValue().set(REVOKED_PREFIX + claims.getId(), "1", remaining);
        } catch (Exception e) {
            log.warn("Redis unavailable, refresh token not revoked: {}", e.getMessage());
        }
    }
}

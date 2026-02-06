package com.example.service;

import io.jsonwebtoken.Claims;
import org.springframework.context.MessageSource;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthUserRepository repository;
    private final JwtUtils jwtUtils;
    private final AuthUserRepository authUserRepository;
    private final UserSessionRedisService userSessionService;
    private final UserSessionRepository userSessionRepository;
    private final MessageSource messageSource;

    public AuthService(PasswordEncoder passwordEncoder, AuthUserRepository repository, JwtUtils jwtUtils, AuthUserRepository authUserRepository, UserSessionRedisService userSessionService, UserSessionRepository userSessionRepository, MessageSource messageSource) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.jwtUtils = jwtUtils;
        this.authUserRepository = authUserRepository;
        this.userSessionService = userSessionService;
        this.userSessionRepository = userSessionRepository;
        this.messageSource = messageSource;
    }


    public LoginResponse login(String username, String password) {

        AuthUser authUser = authUserRepository.findByUsernameAndDeletedFalse(username).orElseThrow(
                () -> new BadCredentialsException("Invalid username or password")
        );

        if (!passwordEncoder.matches(password, authUser.getPassword())) {
            throw new BadCredentialsException(messageSource.getMessage("username.password.error",null,null));
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


}

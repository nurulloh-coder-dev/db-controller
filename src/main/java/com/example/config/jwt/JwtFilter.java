package com.example.config.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.config.CustomUserDetails;
import com.example.model.entity.AuthUser;
import com.example.repository.AuthUserRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final AuthUserRepository authUserRepository;
    private final Set<String> superAdminUsernames;

    public JwtFilter(JwtUtils jwtUtils,
                     AuthUserRepository authUserRepository,
                     @Value("${app-security.super-admins:}") String superAdmins) {
        this.jwtUtils = jwtUtils;
        this.authUserRepository = authUserRepository;
        this.superAdminUsernames = Arrays.stream(superAdmins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                Claims claims = jwtUtils.extractClaims(token);
                if (jwtUtils.isTokenValid(token, claims)) {
                    CustomUserDetails userDetails = prepareUserDetails(claims);

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private CustomUserDetails prepareUserDetails(Claims claims) {
        String username = claims.getSubject();

        AuthUser authUser = authUserRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        String userId = authUser.getId();

        return CustomUserDetails.builder()
                .userId(userId)
                .webLang(authUser.getSettings().getLanguage())
                .username(username)
                .role(authUser.getRole().getName())
                .organizationId(authUser.getOrganization().getId())
                .superAdmin(superAdminUsernames.contains(username))
                .build();
    }
}
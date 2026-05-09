package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.config.jwt.JwtFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth ->
                auth
                        // Allow auth endpoints WITHOUT authentication
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // ADD THIS - Allow agent registration
                        .requestMatchers("/api/v1/agent/**").permitAll()


                        .requestMatchers(HttpMethod.POST, "/api/v1/organization").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/user/organization").permitAll()

                        // All other requests need authentication
                        .anyRequest().authenticated()
        );

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.cors(Customizer.withDefaults());

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 1. Open CORS for /api/v1/agent/** (allow any origin)
        CorsConfiguration agentConfig = new CorsConfiguration();
        agentConfig.setAllowedOriginPatterns(List.of("*"));  // Allows ANY origin
        agentConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        agentConfig.setAllowedHeaders(List.of("*"));
        agentConfig.setAllowCredentials(false);  // MUST be false when using "*"
        agentConfig.setMaxAge(3600L);

        // 2. Restricted CORS for everything else (your current whitelist)
        CorsConfiguration restrictedConfig = new CorsConfiguration();

        restrictedConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:9090",
                "https://coder12345-web.github.io"
        ));
        restrictedConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        restrictedConfig.setAllowedHeaders(List.of("*"));
        restrictedConfig.setAllowCredentials(true);  // Safe because origins are specific
        restrictedConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Register the open config FIRST for the agent paths
        source.registerCorsConfiguration("/api/v1/agent/**", agentConfig);

        // Register the restricted config for everything else
        source.registerCorsConfiguration("/**", restrictedConfig);

        return source;
    }
}
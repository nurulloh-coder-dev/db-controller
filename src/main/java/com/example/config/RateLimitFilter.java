package com.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-client-IP limits on the endpoints that can be abused without (or to obtain) a login:
 * password guessing, reset-email spam and mass sign-ups. In memory, since the backend runs as one instance.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(String method, String path, int limit, Duration window) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule("POST", "/api/v1/auth/login", 10, Duration.ofMinutes(1)),
            new Rule("POST", "/api/v1/auth/forgot-password", 5, Duration.ofMinutes(15)),
            new Rule("POST", "/api/v1/auth/reset-password", 10, Duration.ofMinutes(15)),
            new Rule("POST", "/api/v1/auth/refresh-token", 30, Duration.ofMinutes(1)),
            new Rule("POST", "/api/v1/organization", 5, Duration.ofHours(1)),
            new Rule("POST", "/api/v1/user/organization", 5, Duration.ofHours(1)),
            new Rule("POST", "/api/v1/user/password/check", 10, Duration.ofMinutes(15)),
            new Rule("POST", "/api/v1/user/password", 10, Duration.ofMinutes(15)),
            new Rule("POST", "/api/v1/agent/register", 20, Duration.ofMinutes(1))
    );

    private record Window(long start, int count) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Rule rule = match(request);
        if (rule != null && !allow(rule, clientIp(request))) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(rule.window().toSeconds()));
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\",\"error\":\"Too Many Requests\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private static Rule match(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (Rule rule : RULES) {
            if (rule.method().equals(request.getMethod()) && rule.path().equals(path)) {
                return rule;
            }
        }
        return null;
    }

    private boolean allow(Rule rule, String ip) {
        long now = System.currentTimeMillis();
        long windowMillis = rule.window().toMillis();
        Window w = windows.compute(rule.path() + "|" + ip, (k, old) ->
                old == null || now - old.start() >= windowMillis ? new Window(now, 1) : new Window(old.start(), old.count() + 1));
        if (windows.size() > 100_000) {
            windows.entrySet().removeIf(e -> now - e.getValue().start() >= Duration.ofHours(1).toMillis());
        }
        return w.count() <= rule.limit();
    }

    /**
     * Behind Railway's proxy the client address is the last X-Forwarded-For entry (added by the proxy);
     * earlier entries are whatever the client sent and can't be trusted.
     */
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            return parts[parts.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}

package com.example.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Locks a username for a while after repeated wrong passwords, so a password can't be guessed
 * even when the requests come from many IP addresses. Kept in memory: the backend runs as one instance.
 */
@Service
public class LoginAttemptService {
    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private record Failures(int count, Instant firstFailure) {
    }

    private final Map<String, Failures> failures = new ConcurrentHashMap<>();

    public boolean isLocked(String username) {
        Failures f = failures.get(key(username));
        if (f == null) {
            return false;
        }
        if (f.firstFailure().plus(WINDOW).isBefore(Instant.now())) {
            failures.remove(key(username));
            return false;
        }
        return f.count() >= MAX_FAILURES;
    }

    public void recordFailure(String username) {
        Instant now = Instant.now();
        failures.compute(key(username), (k, f) ->
                f == null || f.firstFailure().plus(WINDOW).isBefore(now) ? new Failures(1, now) : new Failures(f.count() + 1, f.firstFailure()));
        if (failures.size() > 100_000) {
            failures.entrySet().removeIf(e -> e.getValue().firstFailure().plus(WINDOW).isBefore(now));
        }
    }

    public void recordSuccess(String username) {
        failures.remove(key(username));
    }

    private static String key(String username) {
        return username == null ? "" : username.toLowerCase();
    }
}

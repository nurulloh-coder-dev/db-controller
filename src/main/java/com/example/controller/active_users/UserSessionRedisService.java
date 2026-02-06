package com.example.controller.active_users;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserSessionRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration TTL = Duration.ofSeconds(30);
    private final UserSessionRepository userSessionRepository;

    public void markActive(String sessionId) {
        redisTemplate.opsForValue().set("presence:session:" + sessionId, "1", TTL);
    }

    public boolean isActive(String sessionId) {
        return redisTemplate.hasKey("presence:session:" + sessionId);
    }


    public boolean isRedisRunning() {
        try {
            // Using RedisCallback to avoid ambiguity
            String response = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
            return "PONG".equalsIgnoreCase(response);
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * if users grow to hundreds redis set should be created for only active sessions
     */
    private Set<String> getAllActive() {
        Set<String> activeKeys = redisTemplate.keys("presence:session:*");
       return activeKeys
                .stream()
                .map(key->key.replace("presence:session:", ""))
                .collect(Collectors.toSet());
    }

    public void removeSession(String sessionId) {
        redisTemplate.delete("presence:session:" + sessionId);
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void updateLastUserActivity() {
        Set<String> allActive = getAllActive();
        if (allActive.isEmpty()) return;
        userSessionRepository.updateUserLastActivity(Instant.now(), allActive);

    }
}

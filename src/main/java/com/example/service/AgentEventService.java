package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Pushes "you have changes" to connected agents over Server-Sent Events, so agents don't have to poll.
 * Agents keep one outbound connection open (no inbound port needed on their side) and fetch
 * /agent/updates when told. Kept in memory: the backend runs as a single instance.
 */
@Slf4j
@Service
public class AgentEventService {
    // agents reconnect after this; keeps dead connections from piling up
    private static final long STREAM_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();
    // notifications are sent after commit; the short delay batches bursts of changes into one fetch
    private static final long NOTIFY_DELAY_MS = 300;

    private final Map<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Set<String> pending = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "agent-events");
        t.setDaemon(true);
        return t;
    });

    public SseEmitter subscribe(String agentId) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        emitters.computeIfAbsent(agentId, k -> new CopyOnWriteArraySet<>()).add(emitter);
        Runnable remove = () -> remove(agentId, emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            remove.run();
        }
        log.info("Agent {} connected to event stream", agentId);
        return emitter;
    }

    /** Tells the agent (if connected) to fetch its updates. Safe to call many times in a row. */
    public void notifyAgent(String agentId) {
        if (agentId == null || !emitters.containsKey(agentId) || !pending.add(agentId)) {
            return;
        }
        scheduler.schedule(() -> {
            pending.remove(agentId);
            send(agentId, SseEmitter.event().name("changes").data("fetch"));
        }, NOTIFY_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public boolean isConnected(String agentId) {
        return emitters.containsKey(agentId);
    }

    // proxies (Railway included) close connections that stay silent; a comment line keeps them open
    @Scheduled(fixedRate = 20000)
    public void heartbeat() {
        for (String agentId : emitters.keySet()) {
            send(agentId, SseEmitter.event().comment("ping"));
        }
    }

    private void send(String agentId, SseEmitter.SseEventBuilder event) {
        Set<SseEmitter> set = emitters.get(agentId);
        if (set == null) {
            return;
        }
        for (SseEmitter emitter : set) {
            try {
                emitter.send(event);
            } catch (Exception e) {
                remove(agentId, emitter);
            }
        }
    }

    private void remove(String agentId, SseEmitter emitter) {
        emitters.computeIfPresent(agentId, (k, set) -> {
            set.remove(emitter);
            return set.isEmpty() ? null : set;
        });
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
        emitters.values().forEach(set -> set.forEach(SseEmitter::complete));
    }
}

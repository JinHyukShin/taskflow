package com.stockpulse.domain.alert.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterManager.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(30);

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {
        return createEmitter(userId, DEFAULT_TIMEOUT);
    }

    public SseEmitter createEmitter(Long userId, Duration timeout) {
        SseEmitter emitter = new SseEmitter(timeout.toMillis());

        emitter.onCompletion(() -> {
            log.debug("SSE emitter completed for user {}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE emitter timed out for user {}", userId);
            emitters.remove(userId);
        });
        emitter.onError(e -> {
            log.debug("SSE emitter error for user {}: {}", userId, e.getMessage());
            emitters.remove(userId);
        });

        emitters.put(userId, emitter);
        return emitter;
    }

    public void send(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("alert")
                    .data(data));
        } catch (IOException e) {
            log.debug("Failed to send SSE to user {}: {}", userId, e.getMessage());
            emitters.remove(userId);
        }
    }
}

package ru.nuclearius.finam.streamer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatSseEmitterRegistry extends SseEmitterSymbolRegistry {

    @Scheduled(fixedDelay = 15_000)
    public void sendHeartbeat() {
        log.debug("SSE heartbeat");
        forEachEmitter(this::sendHeartbeat);
    }

    private void sendHeartbeat(String symbol, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().comment("heartbeat"));
            log.trace("Heartbeat sent: symbol={}", symbol);
        } catch (Exception ex) {
            log.warn("Heartbeat failed, removing emitter: symbol={}, message={}", symbol, ex.getMessage());
            remove(symbol, emitter);
        }
    }
}
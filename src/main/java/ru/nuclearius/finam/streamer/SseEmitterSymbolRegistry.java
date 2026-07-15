package ru.nuclearius.finam.streamer;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SseEmitterSymbolRegistry {
    private final ConcurrentHashMap<String, Set<SseEmitter>> emittersMap = new ConcurrentHashMap<>();
    private AssetsChangeListener assetsChangeListener;

    public SseEmitter register(String symbol, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersMap.computeIfAbsent(
                symbol,
                key -> new CopyOnWriteArraySet<>());

        boolean isNewKey = emitters.isEmpty();
        emitters.add(emitter);

        log.info("SSE emitter registered: symbol={}, subscribers={}", symbol, emitters.size());

        emitter.onCompletion(() -> {
            log.debug("SSE emitter completed: symbol={}", symbol);
            remove(symbol, emitter);
        });

        if (isNewKey)
            notifyAssetsChange();
        return emitter;
    }

    public void remove(String symbol, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersMap.get(symbol);

        if (emitters != null) {
            emitters.remove(emitter);

            log.debug("SSE emitter removed: symbol={}, remaining={}",
                    symbol, emitters.size());

            if (emitters.isEmpty()) {
                boolean removed = emittersMap.remove(symbol, emitters);

                if (removed) {
                    log.debug("SSE symbol registry cleared: symbol={}", symbol);
                    notifyAssetsChange();
                }
            }
        }
    }

    public Set<String> getSymbols() {
        return Set.copyOf(emittersMap.keySet());
    }

    public void setAssetsChangeListener(AssetsChangeListener listener) {
        this.assetsChangeListener = listener;
    }

    public void removeAll(String symbol) {
        Set<SseEmitter> emitters = emittersMap.remove(symbol);
        if (emitters != null) {
            log.info("Removing all SSE emitters: symbol={}, count={}", symbol, emitters.size());
            emitters.forEach(this::completeEmitter);
            notifyAssetsChange();
        }
    }

    public void clear() {
        var snapshot = new ConcurrentHashMap<>(emittersMap);
        log.info("Clearing all SSE emitters: symbols={}, totalEmitters={}",
                snapshot.size(),
                snapshot.values().stream().mapToInt(Set::size).sum());
        emittersMap.clear();
        snapshot.values().forEach(set -> set.forEach(this::completeEmitter));
        notifyAssetsChange();
    }

    protected Set<SseEmitter> getEmitters(String symbol) {
        return emittersMap.get(symbol);
    }

    protected boolean hasEmitters(String symbol) {
        Collection<SseEmitter> emitters = getEmitters(symbol);
        return emitters != null && !emitters.isEmpty();
    }

    protected void forEachEmitter(BiConsumer<String, SseEmitter> consumer) {
        emittersMap.forEach((symbol, emitters) -> emitters.forEach(emitter -> consumer.accept(symbol, emitter)));
    }

    private void completeEmitter(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ex) {
            log.warn("Failed to complete SSE emitter", ex);
        }
    }

    private void notifyAssetsChange() {
        if (assetsChangeListener != null) {
            assetsChangeListener.onChange(getSymbols());
        }
    }

    public static interface AssetsChangeListener {
        void onChange(Set<String> newSet);
    }

    public void send(String symbol, Set<DataWithMediaType> data) {
        Set<SseEmitter> emitters = getEmitters(symbol);

        if (emitters == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(data);
            } catch (Exception ex) {
                remove(symbol, emitter);
            }
        }
    }
}
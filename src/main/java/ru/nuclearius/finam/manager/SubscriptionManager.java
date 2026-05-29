package ru.nuclearius.finam.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.subscriber.AbstractBackoffObserver;
import ru.nuclearius.finam.subscriber.AbstractControlledObserver;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionManager {
    private final Map<String, AbstractControlledObserver<?, ?>> subscribers = new ConcurrentHashMap<>();

    public void register(String id, AbstractControlledObserver<?, ?> subscriber) {
        subscribers.put(id, subscriber);
        if (subscriber instanceof AbstractBackoffObserver) {
            ((AbstractBackoffObserver<?, ?>) subscriber).setMaxRetryExceededListener(() -> subscribers.remove(id));
        }
    }

    public void stop(String id) {
        AbstractControlledObserver<?, ?> subscriber = subscribers.remove(id);

        if (subscriber != null) {
            log.info("Stopping subscriber with id {}", id);
            subscriber.stop();
        } else {
            log.warn("No subscriber found with id {}", id);
        }
    }

    public boolean isRunning(String id) {
        AbstractControlledObserver<?, ?> observer = subscribers.get(id);
        return observer != null && observer.isRunning();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down subscription manager");

        subscribers.values().forEach(AbstractControlledObserver::stop);
        subscribers.clear();
    }
}

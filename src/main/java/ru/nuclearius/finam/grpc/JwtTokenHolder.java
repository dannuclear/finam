package ru.nuclearius.finam.grpc;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenHolder {
    private final AtomicReference<String> token = new AtomicReference<>();
    private volatile CompletableFuture<String> tokenFuture = new CompletableFuture<>();

    public CompletableFuture<String> awaitToken(Duration timeout) {

        String current = token.get();
        if (current != null) {
            return CompletableFuture.completedFuture(current);
        }

        return tokenFuture.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    public String getToken() {
        return token.get();
    }

    public void setToken(String value) {
        token.set(value);

        if (!tokenFuture.isDone()) {
            tokenFuture.complete(value);
        }
    }
}

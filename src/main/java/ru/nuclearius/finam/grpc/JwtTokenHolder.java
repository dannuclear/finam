package ru.nuclearius.finam.grpc;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenHolder {
    private final AtomicReference<String> token = new AtomicReference<>();

    public String getToken() {
        return token.get();
    }

    public void setToken(String value) {
        token.set(value);
    }
}

package ru.nuclearius.finam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@ConfigurationProperties(prefix = "finam")
public class FinamProperties {

    private Grpc grpc = new Grpc();
    private Rest rest = new Rest();

    @Getter
    @Setter
    public static class Grpc {
        private String host;
        private int port;
        private boolean useTls;
    }

    @Getter
    @Setter
    public static class Rest {
        private String baseUrl;
        private int timeout;
        private String secret;
    }
}

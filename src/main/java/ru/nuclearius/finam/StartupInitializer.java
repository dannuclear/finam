package ru.nuclearius.finam;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import grpc.tradeapi.v1.auth.AuthServiceGrpc.AuthServiceStub;
import grpc.tradeapi.v1.marketdata.MarketDataServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.grpc.JwtTokenHolder;
import ru.nuclearius.finam.manager.SubscriptionManager;
import ru.nuclearius.finam.service.mapper.ProtoMapper;
import ru.nuclearius.finam.streamer.QuoteStreamer;
import ru.nuclearius.finam.subscriber.quotes.QuoteSubscriber;
import ru.nuclearius.finam.subscriber.token.JwtRenewalSubscriber;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class StartupInitializer {

    private final AuthServiceStub authServiceStub;
    private final MarketDataServiceGrpc.MarketDataServiceStub marketDataService;
    private final Environment environment;
    private final JwtTokenHolder tokenHolder;
    private final SubscriptionManager subscriptionManager;
    private final ProtoMapper protoMapper;
    private final QuoteStreamer quoteStreamer;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        JwtRenewalSubscriber subscriber = new JwtRenewalSubscriber(authServiceStub, environment, tokenHolder);

        subscriptionManager.register("jwtRenewal", subscriber);

       subscriber.start();

        // QuoteSubscriber quoteSubscriber = new QuoteSubscriber(marketDataService, protoMapper, quoteStreamer,
        //         quoteStreamer);
        // quoteStreamer.setAssetsChangeListener(quoteSubscriber);
        // subscriptionManager.register("broadcastQuote", subscriber);

        // quoteSubscriber.start();
    }

}

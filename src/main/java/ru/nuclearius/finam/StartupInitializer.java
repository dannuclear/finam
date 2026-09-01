package ru.nuclearius.finam;

import java.time.Duration;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.grpc.JwtTokenHolder;
import ru.nuclearius.finam.service.OrderService;
import ru.nuclearius.finam.streamer.AveragePriceSpreadTrader;
import ru.nuclearius.finam.streamer.QuoteOrderStreamer;
import ru.nuclearius.finam.subscriber.orders.AccountOrdersSubscriber;
import ru.nuclearius.finam.subscriber.quotes.QuoteSingletonSubscriber;
import ru.nuclearius.finam.subscriber.token.JwtRenewalSubscriber;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class StartupInitializer {
    private final JwtTokenHolder tokenHolder;
    private final TaskExecutor singleTaskExecutor;

    private final JwtRenewalSubscriber jwtRenewalSubscriber;
    private final QuoteSingletonSubscriber quoteSubscriber;
    private final AccountOrdersSubscriber accountOrdersSubscriber;

    private final QuoteOrderStreamer quoteOrderStreamer;

    private final OrderService orderService;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        jwtRenewalSubscriber.start();

        tokenHolder.awaitToken(Duration.ofSeconds(5)).thenAcceptAsync(token -> {
            quoteSubscriber.start();
            accountOrdersSubscriber.start();
        }, singleTaskExecutor);

        accountOrdersSubscriber.addListener(quoteOrderStreamer);
        accountOrdersSubscriber.addListener(orderService);
    }

    @PreDestroy
    private void deInit() {
        jwtRenewalSubscriber.stop();
        quoteSubscriber.stop();
        accountOrdersSubscriber.stop();
    }
}

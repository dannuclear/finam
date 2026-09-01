package ru.nuclearius.finam.streamer;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.client.dto.Quote;
import ru.nuclearius.finam.service.domain.OrderState;
import ru.nuclearius.finam.subscriber.orders.AccountOrdersSubscriber.OrderListener;
import ru.nuclearius.finam.subscriber.quotes.QuoteSingletonSubscriber;
import ru.nuclearius.finam.subscriber.quotes.QuoteSingletonSubscriber.QuoteListener;

@Slf4j
@Component
public class QuoteOrderStreamer extends HeartbeatSseEmitterRegistry
        implements QuoteListener, OrderListener {

    private static final String QUOTE_EVENT_NAME = "quote";
    private static final String ORDER_EVENT_NAME = "order";

    public QuoteOrderStreamer(QuoteSingletonSubscriber quoteSubscriber) {
        this.setAssetsChangeListener(symbols -> {
            if (symbols != null && symbols.size() > 0)
                quoteSubscriber.addListener(symbols, this);
            else {
                quoteSubscriber.removeListener(this);
            }
        });
    }

    @Override
    public void onQuote(Quote quote) {
        if (!hasEmitters(quote.getSymbol()))
            return;
        Set<DataWithMediaType> event = SseEmitter.event()
                .name(QUOTE_EVENT_NAME)
                .data(quote)
                .build();
        send(quote.getSymbol(), event);
    }

    @Override
    public void onOrder(OrderState order) {
        if (!hasEmitters(order.getOrder().getSymbol()))
            return;
        Set<DataWithMediaType> event = SseEmitter.event()
                .name(ORDER_EVENT_NAME)
                .data(order)
                .build();
        send(order.getOrder().getSymbol(), event);
    }
}

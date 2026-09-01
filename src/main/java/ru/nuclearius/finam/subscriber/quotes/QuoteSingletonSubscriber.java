package ru.nuclearius.finam.subscriber.quotes;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import grpc.tradeapi.v1.marketdata.MarketDataServiceGrpc;
import grpc.tradeapi.v1.marketdata.SubscribeQuoteRequest;
import grpc.tradeapi.v1.marketdata.SubscribeQuoteResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.client.dto.Quote;
import ru.nuclearius.finam.service.mapper.ProtoMapper;
import ru.nuclearius.finam.subscriber.AbstractBackoffObserver;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteSingletonSubscriber extends AbstractBackoffObserver<SubscribeQuoteRequest, SubscribeQuoteResponse> {

    private final MarketDataServiceGrpc.MarketDataServiceStub marketDataService;
    private final ProtoMapper protoMapper;
    private final Map<String, List<QuoteListener>> listenerMap = new ConcurrentHashMap<>();

    @Override
    protected synchronized void subscribe() {
        if (listenerMap.isEmpty()) {
            log.info("Symbol listeners is empty");
            return;
        }
        Set<String> symbols = listenerMap.keySet();
        log.info("Subscribing to {} symbols: {}", symbols.size(), symbols);
        SubscribeQuoteRequest request = SubscribeQuoteRequest.newBuilder()
                .addAllSymbols(symbols)
                .build();
        marketDataService.subscribeQuote(request, this);
    }

    @Override
    public void onNext(SubscribeQuoteResponse response) {
        protoMapper.toDomain(response)
                .getQuotes()
                .forEach(quote -> {
                    List<QuoteListener> listeners = listenerMap.get(quote.getSymbol());

                    if (listeners == null || listeners.isEmpty()) {
                        return;
                    }

                    for (QuoteListener listener : listeners) {
                        try {
                            listener.onQuote(quote);
                        } catch (Exception e) {
                            log.error("Quote listener failed for symbol {}", quote.getSymbol(), e);
                        }
                    }
                });
    }

    public void addListener(Collection<String> symbols, QuoteListener listener) {
        Objects.requireNonNull(symbols, "Symbols must be present");
        Objects.requireNonNull(listener, "Quote listener must be present");

        boolean newSymbol = false;

        for (String symbol : symbols) {
            Objects.requireNonNull(symbol, "Symbol must be present");

            if (listenerMap.putIfAbsent(symbol, new CopyOnWriteArrayList<>()) == null) {
                newSymbol = true;
            }

            listenerMap.get(symbol).add(listener);
        }

        if (newSymbol && isRunning()) {
            resubscribe();
        }
    }

    public void removeListener(String symbol, QuoteListener listener) {
        List<QuoteListener> listeners = listenerMap.get(symbol);
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
        if (!listeners.isEmpty()) {
            return;
        }
        listenerMap.remove(symbol);
        if (isRunning()) {
            resubscribe();
        }
    }

    public void removeListener(QuoteListener listener) {
        Objects.requireNonNull(listener, "Quote listener must be present");

        boolean symbolsChanged = false;

        for (Iterator<Map.Entry<String, List<QuoteListener>>> it = listenerMap.entrySet().iterator(); it
                .hasNext();) {

            Map.Entry<String, List<QuoteListener>> entry = it.next();
            List<QuoteListener> listeners = entry.getValue();

            listeners.remove(listener);

            if (listeners.isEmpty()) {
                listenerMap.remove(entry.getKey(), listeners);
                symbolsChanged = true;
            }
        }

        if (symbolsChanged && isRunning()) {
            resubscribe();
        }
    }

    private synchronized void resubscribe() {
        StreamObserver<?> observer = getRequestStream();

        if (observer != null) {
            cancel("change assets");
        }

        subscribe();
    }

    public interface QuoteListener {
        void onQuote(Quote quote);
    }
}

package ru.nuclearius.finam.subscriber.quotes;

import java.util.Set;

import org.springframework.stereotype.Component;

import grpc.tradeapi.v1.marketdata.MarketDataServiceGrpc;
import grpc.tradeapi.v1.marketdata.SubscribeQuoteRequest;
import grpc.tradeapi.v1.marketdata.SubscribeQuoteResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.client.dto.Quote;
import ru.nuclearius.finam.service.mapper.ProtoMapper;
import ru.nuclearius.finam.streamer.SseEmitterSymbolRegistry.AssetsChangeListener;
import ru.nuclearius.finam.subscriber.AbstractBackoffObserver;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteSubscriber extends AbstractBackoffObserver<SubscribeQuoteRequest, SubscribeQuoteResponse>
        implements AssetsChangeListener {

    private final MarketDataServiceGrpc.MarketDataServiceStub marketDataService;
    private final ProtoMapper protoMapper;
    private final QuoteListener quoteListener;
    private final SymbolsProvider symbolSource;

    @Override
    protected void subscribe() {
        Set<String> symbols = symbolSource.getSymbols();
        if (symbols == null || symbols.size() == 0) {
            log.info("Symbols empty");
            return;
        }

        log.info("Subscribing to {} symbols: {}", symbols.size(), symbols);

        SubscribeQuoteRequest request = SubscribeQuoteRequest.newBuilder()
                .addAllSymbols(symbols)
                .build();
        marketDataService.subscribeQuote(request, this);
    }

    @Override
    public void onNext(SubscribeQuoteResponse response) {
        protoMapper.toDomain(response).getQuotes().forEach(quoteListener::onQuote);
    }

    @Override
    public void onChange(Set<String> newSet) {
        StreamObserver<?> observer = getRequestStream();
        cancel("change assets");
        if (observer == null) {
            subscribe();
        }
    }

    public interface QuoteListener {
        void onQuote(Quote quote);
    }

    public interface SymbolsProvider {
        Set<String> getSymbols();
    }
}

package ru.nuclearius.finam.subscriber.quotes;

import org.springframework.stereotype.Component;

import grpc.tradeapi.v1.marketdata.MarketDataServiceGrpc;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.service.mapper.ProtoMapper;

@Component
@RequiredArgsConstructor
public class QuoteSubscriberFactory {

    private final MarketDataServiceGrpc.MarketDataServiceStub marketDataService;
    private final ProtoMapper protoMapper;

    public QuoteSubscriber create(QuoteSubscriber.QuoteListener listener, QuoteSubscriber.SymbolsProvider source) {
        return new QuoteSubscriber(marketDataService, protoMapper, listener, source);
    }
}

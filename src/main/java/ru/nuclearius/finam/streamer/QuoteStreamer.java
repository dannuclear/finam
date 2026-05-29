package ru.nuclearius.finam.streamer;

import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ru.nuclearius.finam.client.dto.Quote;
import ru.nuclearius.finam.subscriber.quotes.QuoteSubscriber.QuoteListener;
import ru.nuclearius.finam.subscriber.quotes.QuoteSubscriber.SymbolsProvider;

@Component
@Scope("singleton")
public class QuoteStreamer extends HeartbeatSseEmitterRegistry implements QuoteListener, SymbolsProvider {

    public void sendQuote(String symbol, Quote quote) {
        Set<SseEmitter> emitters = getEmitters(symbol);

        if (emitters == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("quote").data(quote).build());
            } catch (Exception ex) {
                remove(symbol, emitter);
            }
        }
    }

    @Override
    public void onQuote(Quote quote) {
        sendQuote(quote.getSymbol(), quote);
    }
}

package ru.nuclearius.finam.streamer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.ConcurrentBarSeries;
import org.ta4j.core.ConcurrentBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.bars.TimeBarBuilderFactory;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.JustOnceRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import com.fasterxml.jackson.annotation.JsonProperty;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.client.dto.Account;
import ru.nuclearius.finam.client.dto.Account.Position;
import ru.nuclearius.finam.client.dto.Quote;
import ru.nuclearius.finam.service.BarService;
import ru.nuclearius.finam.service.FinamService;
import ru.nuclearius.finam.service.OrderService;
import ru.nuclearius.finam.service.domain.Order;
import ru.nuclearius.finam.service.domain.Order.Side;
import ru.nuclearius.finam.subscriber.quotes.QuoteSingletonSubscriber;
import ru.nuclearius.finam.subscriber.quotes.QuoteSingletonSubscriber.QuoteListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AveragePriceSpreadTrader extends HeartbeatSseEmitterRegistry implements QuoteListener {
    private Set<String> symbols;
    private final Integer fastMaBarCount = 4;
    private Map<String, AssetOptions> assetMap;
    private final int averageDaysCount = 6;

    private final BarService barService;
    private final QuoteSingletonSubscriber quoteSubscriber;
    private final OrderService orderService;
    private final FinamService finamService;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private static final String QUOTE_EVENT_NAME = "quote";
    private Map<String, Map<String, Rule>> rules;

    public Set<String> getSpreadSymbols() {
        return this.symbols;
    }

    public Boolean isRunning() {
        return isRunning.get();
    }

    public void start(Set<String> symbols) {
        Instant now = Instant.now();
        assetMap = symbols.stream().map(s -> barService
                .ta4jConcurrentSeriesAsync(s, TimeFrame.TIME_FRAME_D, now.minus(Duration.ofDays(averageDaysCount - 1)),
                        now)
                .thenApply(averageBarSeries -> {
                    averageBarSeries.setMaximumBarCount(averageDaysCount);
                    ClosePriceIndicator averageClosePriceIndicator = new ClosePriceIndicator(averageBarSeries);
                    Indicator<Num> slowMaIndicator = new SMAIndicator(averageClosePriceIndicator, averageDaysCount);
                    ConcurrentBarSeries barSeries = new ConcurrentBarSeriesBuilder()
                            .withName(s + "-live-series")
                            .withBarBuilderFactory(new TimeBarBuilderFactory(Duration.ofMinutes(1), true))
                            .build();

                    Indicator<Num> normalizedIndicator = NumericIndicator.closePrice(barSeries)
                            .dividedBy(new CachedIndicator<Num>(barSeries) {

                                @Override
                                public int getCountOfUnstableBars() {
                                    return slowMaIndicator.getCountOfUnstableBars();
                                }

                                @Override
                                protected Num calculate(int index) {
                                    BarSeries barSeries = slowMaIndicator.getBarSeries();
                                    return slowMaIndicator.getValue(barSeries.getEndIndex());
                                }
                            })
                            .minus(1)
                            .multipliedBy(100.0);
                    Indicator<Num> fastMaIndicator = new SMAIndicator(normalizedIndicator, fastMaBarCount);
                    Indicator<Num> offsetIndicator = NumericIndicator.of(fastMaIndicator).minus(0.2);

                    return new AssetOptions(s, barSeries, fastMaIndicator, slowMaIndicator, normalizedIndicator,
                            offsetIndicator);
                }))
                .map(CompletableFuture::join).collect(Collectors.toMap(AssetOptions::symbol, Function.identity()));

        rules = assetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        first -> assetMap.entrySet().stream()
                                .filter(second -> !second.getKey().equals(first.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, second -> {

                                    return new JustOnceRule(
                                            new UnderIndicatorRule(first.getValue().fastMaIndicator(),
                                                    new LastValueIndicator(second.getValue().offsetIndicator())) {
                                                @Override
                                                public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                                                    if (index < fastMaBarCount)
                                                        return false;
                                                    return super.isSatisfied(index, tradingRecord);
                                                }
                                            });
                                }))));
        this.symbols = symbols;
        quoteSubscriber.addListener(symbols, this);
        isRunning.set(true);
    }

    public void stop() {
        quoteSubscriber.removeListener(this);
        isRunning.set(false);
    }

    public void subscribe(SseEmitter sseEmitter) {
        if (this.isRunning.get())
            symbols.forEach(s -> register(s, sseEmitter));
    }

    @Override
    public void onQuote(Quote quote) {
        if (quote.getLast() == null || quote.getLastSize() == null)
            return;
        // log.info("{}", quote);
        String symbol = quote.getSymbol();
        AssetOptions option = assetMap.get(symbol);
        ConcurrentBarSeries series = option.barSeries();

        series.ingestTrade(quote.getTimestamp(), quote.getLastSize(), quote.getLast());

        Bar lastBar = series.getLastBar();

        BigDecimal normalizedValue = getIndicatorLastValue(option.normalizedIndicator());
        BigDecimal fastMaValue = getIndicatorLastValue(option.fastMaIndicator());
        BigDecimal offsetValue = getIndicatorLastValue(option.offsetIndicator());

        if (!orderService.hasChains()) {
            rules.get(symbol).entrySet().forEach(entry -> {
                if (entry.getValue().isSatisfied(series.getEndIndex())) {
                    log.info("{}: {} пересекает вниз {}", lastBar.getEndTime(), symbol, entry.getKey());
                    Account account = finamService.getAccount("2029595");
                    Optional<Position> sellPositionOptional = account.getPositions().stream()
                            .filter(p -> entry.getKey().equals(p.getSymbol()))
                            .findFirst();
                    if (sellPositionOptional.isPresent()) {
                        Bar targerBar = assetMap.get(symbol).barSeries().getLastBar();
                        createRebalanceChain(symbol, entry.getKey(), sellPositionOptional.get(), targerBar);
                    }
                }
            });
        }

        StreamingData data = new StreamingData(symbol, lastBar.getEndTime(), normalizedValue, fastMaValue, offsetValue);
        sendToEmitters(symbol, data);
    }

    private void sendToEmitters(String symbol, StreamingData data) {
        if (!hasEmitters(symbol))
            return;
        Set<DataWithMediaType> event = SseEmitter.event()
                .name(QUOTE_EVENT_NAME)
                .data(data)
                .build();
        send(symbol, event);
    }

    private BigDecimal getIndicatorLastValue(Indicator<Num> indicator) {
        int index = indicator.getBarSeries().getEndIndex();
        Num value = indicator.getValue(index);
        return value.bigDecimalValue().setScale(3, RoundingMode.HALF_UP);
    }

    private void createRebalanceChain(
            String buySymbol,
            String sellSymbol,
            Position sellPosition,
            Bar lastBar) {

        Bar targetBar = assetMap.get(buySymbol)
                .barSeries()
                .getLastBar();

        BigDecimal amount = sellPosition.getCurrentPrice()
                .multiply(sellPosition.getQuantity());

        BigDecimal buyQuantity = amount.divide(
                targetBar.getClosePrice().bigDecimalValue(),
                0,
                RoundingMode.DOWN);

        if (buyQuantity.signum() == 0) {
            return;
        }

        log.info(
                "{}: {} -> {}: sell={}, buy={}",
                lastBar.getEndTime(),
                sellSymbol,
                buySymbol,
                sellPosition.getQuantity(),
                buyQuantity);

        orderService.createChain(List.of(
                Order.builder()
                        .symbol(sellSymbol)
                        .side(Side.SIDE_SELL)
                        .quantity(sellPosition.getQuantity())
                        .type(Order.Type.ORDER_TYPE_MARKET)
                        .timeInForce(Order.TimeInForce.TIME_IN_FORCE_DAY)
                        .build(),

                Order.builder()
                        .symbol(buySymbol)
                        .side(Side.SIDE_BUY)
                        .quantity(buyQuantity)
                        .type(Order.Type.ORDER_TYPE_MARKET)
                        .timeInForce(Order.TimeInForce.TIME_IN_FORCE_DAY)
                        .build()));
    }

    private record AssetOptions(
            String symbol,
            ConcurrentBarSeries barSeries,
            Indicator<Num> fastMaIndicator,
            Indicator<Num> slowMaIndicator,
            Indicator<Num> normalizedIndicator,
            Indicator<Num> offsetIndicator) {
    }

    private record StreamingData(
            String symbol,
            Instant timestamp,
            BigDecimal last,
            BigDecimal fastMa,
            BigDecimal offset) {

        @JsonProperty
        public long seconds() {
            return timestamp.getEpochSecond();
        }
    }
}

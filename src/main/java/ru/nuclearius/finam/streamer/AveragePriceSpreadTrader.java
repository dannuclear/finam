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
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ta4j.core.Bar;
import org.ta4j.core.ConcurrentBarSeries;
import org.ta4j.core.ConcurrentBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.bars.TimeBarBuilderFactory;
import org.ta4j.core.indicators.averages.SMAIndicator;
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
import ru.nuclearius.finam.ta4j.indicator.LastAverageIndicator;
import ru.nuclearius.finam.ta4j.indicator.NormalizedPriceIndicator;
import ru.nuclearius.finam.utils.DateUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AveragePriceSpreadTrader extends HeartbeatSseEmitterRegistry implements QuoteListener {
    private Set<String> symbols;
    private Map<String, AssetOptions> assetMap;

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

    public void start(Set<String> symbols, Integer averageDaysCount, Integer fastMaBarCount, Double spread) {
        Assert.notNull(fastMaBarCount, "Количество баров быстрой средней не указано");
        Assert.notNull(averageDaysCount, "Количество дней средней не указано");
        Assert.notNull(spread, "Спред не указан");

        ConcurrentBarSeriesBuilder slowSeriesBuilder = new ConcurrentBarSeriesBuilder()
                .withBarBuilderFactory(new TimeBarBuilderFactory(Duration.ofDays(1), true));
        ConcurrentBarSeriesBuilder liveSeriesBuilder = new ConcurrentBarSeriesBuilder()
                .withBarBuilderFactory(new TimeBarBuilderFactory(Duration.ofMinutes(1), true));

        assetMap = symbols.stream().map(symbol -> {
            ConcurrentBarSeries slowSeries = slowSeriesBuilder.withName(symbol + "-slow-series")
                    .build();
            LastAverageIndicator slowMaIndicator = LastAverageIndicator.of(slowSeries, averageDaysCount);
            ConcurrentBarSeries liveSeries = liveSeriesBuilder.withName(symbol + "-live-series")
                    .build();
            NormalizedPriceIndicator nPriceIndicator = new NormalizedPriceIndicator(liveSeries,
                    slowMaIndicator);
            Indicator<Num> fastMaIndicator = new SMAIndicator(nPriceIndicator, fastMaBarCount);
            Indicator<Num> offsetIndicator = NumericIndicator.of(fastMaIndicator).minus(spread);

            AssetOptions options = new AssetOptions(
                    symbol,
                    liveSeries,
                    slowMaIndicator,
                    nPriceIndicator,
                    fastMaIndicator,
                    offsetIndicator);

            return Pair.of(symbol, options);
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        rules = assetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        first -> assetMap.entrySet().stream()
                                .filter(second -> !second.getKey().equals(first.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, second -> {

                                    return new JustOnceRule(
                                            new UnderIndicatorRule(first.getValue().fastMaIndicator(),
                                                    new LastValueIndicator(second.getValue().offsetIndicator(), 1)) {
                                                @Override
                                                public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                                                    if (index < fastMaBarCount)
                                                        return false;
                                                    return super.isSatisfied(index, tradingRecord);
                                                }
                                            });
                                }))));

        Duration slowDuration = DateUtils.toDuration(TimeFrame.TIME_FRAME_D);
        Instant now = Instant.now();
        Instant end = now.minus(slowDuration);
        Instant start = end.minus(slowDuration.multipliedBy(averageDaysCount - 1));

        List<CompletableFuture<Void>> features = assetMap.entrySet().stream()
                .map(e -> barService.ta4jConcurrentSeriesAsync(e.getKey(), TimeFrame.TIME_FRAME_D, start, end)
                        .thenAccept(series -> {
                            e.getValue().slowMaIndicator().update(series.getBarData());
                        }))
                .toList();
        features.forEach(CompletableFuture::join);

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

        if (series.getEndIndex() == -1 || !quote.getTimestamp().isBefore(series.getLastBar().getBeginTime()))
            series.ingestTrade(quote.getTimestamp(), quote.getLastSize(), quote.getLast());

        NormalizedPriceIndicator normalizedOnSlowMaIndicator = option.normalizedOnSlowMaIndicator();
        BigDecimal normalizedValue = getIndicatorLastValue(normalizedOnSlowMaIndicator);
        BigDecimal fastMaValue = getIndicatorLastValue(option.fastMaIndicator());
        BigDecimal offsetValue = getIndicatorLastValue(option.offsetIndicator());

        Bar lastBar = series.getLastBar();
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

        BigDecimal targetPrice = targetBar.getClosePrice().bigDecimalValue();
        BigDecimal buyQuantity = amount.divide(
                targetPrice,
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
            LastAverageIndicator slowMaIndicator,
            NormalizedPriceIndicator normalizedOnSlowMaIndicator,
            Indicator<Num> fastMaIndicator,
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

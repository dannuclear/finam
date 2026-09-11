package ru.nuclearius.finam.streamer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.ConcurrentBarSeries;
import org.ta4j.core.ConcurrentBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.bars.TimeBarBuilderFactory;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.CombineIndicator;
import org.ta4j.core.indicators.numeric.BinaryOperationIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

import com.fasterxml.jackson.annotation.JsonProperty;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.client.dto.Account.Position;
import ru.nuclearius.finam.client.dto.Quote;
import ru.nuclearius.finam.service.BarService;
import ru.nuclearius.finam.service.OrderService;
import ru.nuclearius.finam.service.domain.Order;
import ru.nuclearius.finam.service.domain.Order.Side;
import ru.nuclearius.finam.subscriber.account.AccountInfoSubscriber;
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
    private Map<String, AssetData> assetMap;

    private final BarService barService;
    private final QuoteSingletonSubscriber quoteSubscriber;
    private final OrderService orderService;
    private final AccountInfoSubscriber accountInfoSubscriber;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private static final String EMMITTER_KEY = "spread-trader";
    private Double spread;
    // private Map<String, Map<String, Rule>> rules;

    @Scheduled(fixedDelay = 5_000)
    private void process() {
        if (isRunning.get() && assetMap != null) {
            String bestSellSymbol = null;
            String bestBuySymbol = null;
            Double bestSpread = 0.0;
            Position toSellPosition = null;
            Map<String, EmitterData> emitterData = new HashMap<>(assetMap.size() * 3);
            for (Map.Entry<String, AssetData> e : assetMap.entrySet()) {
                String toBuySymbol = e.getKey();
                AssetData assetData = e.getValue();
                BarSeries barSeries = assetData.getBarSeries();
                Integer endIndex = barSeries.getEndIndex();
                List<Position> positions = accountInfoSubscriber.getPositions();
                Map<String, Indicator<Num>> spreadIndicators = assetData.getSpreadIndicators();
                // Map<String, Rule> pairedRules = rules.get(toBuySymbol);
                for (Map.Entry<String, Indicator<Num>> sellEntry : spreadIndicators.entrySet()) {
                    String toSellSymbol = sellEntry.getKey();
                    Indicator<Num> spreadIndicator = sellEntry.getValue();
                    Double pairSpread = spreadIndicator.getValue(endIndex).doubleValue();
                    if (-pairSpread > spread) {
                        log.debug("Спред {} между дешевой {} и дорогой {}", pairSpread, toBuySymbol, toSellSymbol);
                        Position position = positions.stream()
                                .filter(p -> p.getSymbol().equals(toSellSymbol)).findFirst().orElse(null);
                        if (positions != null && !orderService.hasChains() && position != null
                                && position.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                            log.debug("Продаем {} покупаем {}", toSellSymbol, toBuySymbol);
                            if (pairSpread < bestSpread) {
                                bestSellSymbol = toSellSymbol;
                                bestBuySymbol = toBuySymbol;
                                bestSpread = pairSpread;
                                toSellPosition = position;
                            }
                        } else {
                            log.debug("Позиции нет");
                        }
                    }
                }
                Bar lastBar = barSeries.getBar(endIndex);
                emitterData.put(toBuySymbol, new EmitterData(
                        lastBar.getEndTime(),
                        assetData.getNormalizedOnSlowMaIndicator().getValue(endIndex).bigDecimalValue()));
                emitterData.put(toBuySymbol + "-fast-ma", new EmitterData(
                        lastBar.getEndTime(),
                        assetData.getFastMaIndicator().getValue(endIndex).bigDecimalValue()));
                emitterData.put(toBuySymbol + "-offset-ma", new EmitterData(
                        lastBar.getEndTime(),
                        assetData.getOffsetIndicator().getValue(endIndex).bigDecimalValue()));
            }

            if (bestBuySymbol != null && bestSellSymbol != null && bestSpread < 0 && toSellPosition != null && !orderService.hasChains()) {
                log.info("Лучший спред {} продать {} купить {}", bestSpread, bestSellSymbol, bestBuySymbol);
                createRebalanceChain(bestBuySymbol, bestSellSymbol, toSellPosition);
            }

            if (hasEmitters(EMMITTER_KEY)) {
                Set<DataWithMediaType> event = SseEmitter.event()
                        .name(EMMITTER_KEY)
                        .data(emitterData)
                        .build();
                send(EMMITTER_KEY, event);
            }
        }
    }

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
                .withMaxBarCount(averageDaysCount)
                .withBarBuilderFactory(new TimeBarBuilderFactory(Duration.ofDays(1), true));
        ConcurrentBarSeriesBuilder liveSeriesBuilder = new ConcurrentBarSeriesBuilder()
                .withMaxBarCount(100)
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

            AssetData options = new AssetData(
                    liveSeries,
                    slowMaIndicator,
                    nPriceIndicator,
                    fastMaIndicator,
                    offsetIndicator);

            return Pair.of(symbol, options);
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        assetMap.entrySet().stream()
                .forEach(entry -> {
                    String firstSymbol = entry.getKey();
                    AssetData data = entry.getValue();
                    data.setSpreadIndicators(assetMap.entrySet().stream()
                            .filter(second -> !second.getKey().equals(firstSymbol))
                            .collect(Collectors.toMap(Map.Entry::getKey,
                                    second -> CombineIndicator.minus(
                                            data.getFastMaIndicator(),
                                            LastValueMinusOffsetIndicator.of(second.getValue().getFastMaIndicator(),
                                                    1)))));
                });

        Duration slowDuration = DateUtils.toDuration(TimeFrame.TIME_FRAME_D);
        Instant now = Instant.now();
        Instant end = now.minus(slowDuration);
        Instant start = end.minus(slowDuration.multipliedBy(averageDaysCount - 1));

        List<CompletableFuture<Void>> features = assetMap.entrySet().stream()
                .map(e -> barService.ta4jConcurrentSeriesAsync(e.getKey(), TimeFrame.TIME_FRAME_D, start, end)
                        .thenAccept(series -> {
                            e.getValue().getSlowMaIndicator().update(series.getBarData());
                        }))
                .toList();
        features.forEach(CompletableFuture::join);

        List<CompletableFuture<Void>> fastFeatures = assetMap.entrySet().stream()
                .map(e -> barService
                        .ta4jConcurrentSeriesAsync(e.getKey(), TimeFrame.TIME_FRAME_M1,
                                now.minus(Duration.ofMinutes(10)), now)
                        .thenAccept(series -> series.getBarData().forEach(b -> e.getValue().getBarSeries().addBar(b))))
                .toList();
        fastFeatures.forEach(CompletableFuture::join);

        this.spread = spread;
        this.symbols = symbols;
        quoteSubscriber.addListener(symbols, this);
        isRunning.set(true);
    }

    public void stop() {
        quoteSubscriber.removeListener(this);
        removeAll(EMMITTER_KEY);
        isRunning.set(false);
    }

    public void subscribe(SseEmitter sseEmitter) {
        if (isRunning.get())
            register(EMMITTER_KEY, sseEmitter);
    }

    @Override
    public void onQuote(Quote quote) {
        if (quote.getLast() == null || quote.getLastSize() == null)
            return;
        String symbol = quote.getSymbol();
        AssetData option = assetMap.get(symbol);
        ConcurrentBarSeries series = option.getBarSeries();

        if (series.getEndIndex() == -1 || !quote.getTimestamp().isBefore(series.getLastBar().getBeginTime()))
            series.ingestTrade(quote.getTimestamp(), quote.getLastSize(), quote.getLast());
    }

    private void createRebalanceChain(String buySymbol, String sellSymbol, Position sellPosition) {
        Bar targetBar = assetMap.get(buySymbol)
                .getBarSeries()
                .getLastBar();

        BigDecimal amount = sellPosition.getCurrentPrice()
                .multiply(sellPosition.getQuantity());

        BigDecimal targetPrice = targetBar.getClosePrice().bigDecimalValue();
        BigDecimal buyQuantity = amount.divide(
                targetPrice,
                0,
                RoundingMode.DOWN).add(BigDecimal.ONE);

        if (buyQuantity.signum() == 0) {
            return;
        }

        log.info("{}: {} -> {}: sell={}, buy={}", sellSymbol, buySymbol, sellPosition.getQuantity(), buyQuantity);

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

    @Getter
    @Setter
    @RequiredArgsConstructor
    private class AssetData {
        private final ConcurrentBarSeries barSeries;
        private final LastAverageIndicator slowMaIndicator;
        private final NormalizedPriceIndicator normalizedOnSlowMaIndicator;
        private final Indicator<Num> fastMaIndicator;
        private final Indicator<Num> offsetIndicator;
        private Map<String, Indicator<Num>> spreadIndicators;
    }

    private record EmitterData(
            Instant timestamp,
            BigDecimal value) {

        @JsonProperty
        public long seconds() {
            return timestamp.getEpochSecond();
        }
    }
}

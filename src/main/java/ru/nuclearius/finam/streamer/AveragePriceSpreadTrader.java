package ru.nuclearius.finam.streamer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

import com.fasterxml.jackson.annotation.JsonProperty;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.RequiredArgsConstructor;
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
import ru.nuclearius.finam.ta4j.rule.LastValueUnderIndicatorRule;
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
    private Map<String, Map<String, Rule>> rules;

    @Scheduled(fixedDelay = 5_000)
    private void process() {
        if (isRunning.get() && assetMap != null) {
            Map<String, EmitterData> emitterData = new HashMap<>(assetMap.size() * 3);
            for (Map.Entry<String, AssetData> e : assetMap.entrySet()) {
                String toBuySymbol = e.getKey();
                AssetData assetData = e.getValue();
                BarSeries barSeries = assetData.barSeries();
                Integer endIndex = barSeries.getEndIndex();
                List<Position> positions = accountInfoSubscriber.getPositions();
                Map<String, Rule> pairedRules = rules.get(toBuySymbol);
                for (Map.Entry<String, Rule> ruleEntry : pairedRules.entrySet()) {
                    String toSellSymbol = ruleEntry.getKey();
                    Rule rule = ruleEntry.getValue();
                    if (rule.isSatisfied(endIndex)) {
                        log.info("Спред между дешевой {} и дорогой {}", toBuySymbol, toSellSymbol);
                        Optional<Position> positionOpt = positions.stream()
                                .filter(p -> p.getSymbol().equals(toSellSymbol)).findFirst();
                        if (positions != null && !orderService.hasChains() && positionOpt.isPresent()) {
                            log.info("Продаем {} покупаем {}", toSellSymbol, toBuySymbol);
                            createRebalanceChain(toBuySymbol, toSellSymbol, positionOpt.get());
                        } else {
                            log.info("Позиции нет");
                        }
                    }
                }
                Bar lastBar = barSeries.getBar(endIndex);
                emitterData.put(toBuySymbol, new EmitterData(
                        lastBar.getEndTime(),
                        assetData.normalizedOnSlowMaIndicator().getValue(endIndex).bigDecimalValue()));
                emitterData.put(toBuySymbol + "-fast-ma", new EmitterData(
                        lastBar.getEndTime(),
                        assetData.fastMaIndicator().getValue(endIndex).bigDecimalValue()));
                emitterData.put(toBuySymbol + "-offset-ma", new EmitterData(
                        lastBar.getEndTime(),
                        assetData.offsetIndicator().getValue(endIndex).bigDecimalValue()));
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

        rules = assetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        first -> assetMap.entrySet().stream()
                                .filter(second -> !second.getKey().equals(first.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, second -> {
                                    return LastValueUnderIndicatorRule.of(
                                            first.getValue().fastMaIndicator(),
                                            second.getValue().offsetIndicator(),
                                            1);
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
        ConcurrentBarSeries series = option.barSeries();

        if (series.getEndIndex() == -1 || !quote.getTimestamp().isBefore(series.getLastBar().getBeginTime()))
            series.ingestTrade(quote.getTimestamp(), quote.getLastSize(), quote.getLast());
    }

    private void createRebalanceChain(String buySymbol, String sellSymbol, Position sellPosition) {
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

    private record AssetData(
            ConcurrentBarSeries barSeries,
            LastAverageIndicator slowMaIndicator,
            NormalizedPriceIndicator normalizedOnSlowMaIndicator,
            Indicator<Num> fastMaIndicator,
            Indicator<Num> offsetIndicator) {
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

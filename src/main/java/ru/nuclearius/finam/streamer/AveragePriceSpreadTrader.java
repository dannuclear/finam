package ru.nuclearius.finam.streamer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
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
import org.ta4j.core.bars.TimeBarBuilderFactory;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.DecimalNum;
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
import ru.nuclearius.finam.ta4j.indicator.SpreadIndicator;
import ru.nuclearius.finam.utils.DateUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AveragePriceSpreadTrader extends HeartbeatSseEmitterRegistry implements QuoteListener {
    private Set<String> symbols;
    @Getter
    private Map<String, AssetData> assetIndicators;

    private final BarService barService;
    private final QuoteSingletonSubscriber quoteSubscriber;
    private final OrderService orderService;
    private final AccountInfoSubscriber accountInfoSubscriber;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private static final String EMMITTER_KEY = "spread-trader";
    private Double spread;
    private Set<PairIndicator> pairIndicators;

    @Scheduled(fixedDelay = 5_000)
    private void process() {
        if (isRunning.get() && assetIndicators != null) {

            Num numSpread = DecimalNum.valueOf(spread);
            List<PairSpreadValue> pairSpreadValues = pairIndicators.stream()
                    .map(pi -> PairSpreadValue.of(pi.left(), pi.right(), pi.indicator()))
                    .filter(pi -> pi.value() != null && !pi.value().isNaN() && pi.value().abs().isGreaterThan(numSpread))
                    .sorted(Comparator.comparing(PairSpreadValue::value).reversed())
                    .toList();

            if (log.isDebugEnabled())
                for (PairSpreadValue pair : pairSpreadValues) {
                    BigDecimal value = pair.value().bigDecimalValue().setScale(4, RoundingMode.HALF_UP);
                    log.info("Спред {} между дорогой {} и дешевой {}", value, pair.toSell(), pair.toBuy());
                }

            List<Position> positions = accountInfoSubscriber.getPositions();
            Optional<PairSpreadValue> bestOpt = pairSpreadValues.stream()
                    .filter(pair -> positions.stream().anyMatch(pos -> pair.toSell().equals(pos.getSymbol())))
                    .findFirst();

            if (bestOpt.isPresent() && !orderService.hasChains()) {
                PairSpreadValue best = bestOpt.get();
                Optional<Position> position = positions.stream().filter(pos -> best.toSell().equals(pos.getSymbol())).findFirst();
                log.info("Лучший спред {} продать {} купить {}", best.value(), best.toSell(), best.toBuy());
                createRebalanceChain(best.toBuy(), best.toSell(), position.get());
            }

            if (hasEmitters(EMMITTER_KEY))
                sendToEmiters();
        }
    }

    private void sendToEmiters() {
        Map<String, EmitterData> emitterData = new HashMap<>(assetIndicators.size() * 3);
        for (Map.Entry<String, AssetData> e : assetIndicators.entrySet()) {
            String toBuySymbol = e.getKey();
            AssetData assetData = e.getValue();
            BarSeries barSeries = assetData.getBarSeries();
            Integer endIndex = barSeries.getEndIndex();
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

        Set<DataWithMediaType> event = SseEmitter.event()
                .name(EMMITTER_KEY)
                .data(emitterData)
                .build();
        send(EMMITTER_KEY, event);

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

        buildIndicators(symbols, averageDaysCount, fastMaBarCount, spread);

        buildPairs();

        infillBars(averageDaysCount);

        this.spread = spread;
        this.symbols = symbols;
        quoteSubscriber.addListener(symbols, this);
        isRunning.set(true);
    }

    private void buildIndicators(Set<String> symbols, Integer averageDaysCount, Integer fastMaBarCount, Double spread) {
        ConcurrentBarSeriesBuilder slowSeriesBuilder = new ConcurrentBarSeriesBuilder()
                .withMaxBarCount(averageDaysCount)
                .withBarBuilderFactory(new TimeBarBuilderFactory(Duration.ofDays(1), true));
        ConcurrentBarSeriesBuilder liveSeriesBuilder = new ConcurrentBarSeriesBuilder()
                .withMaxBarCount(100)
                .withBarBuilderFactory(new TimeBarBuilderFactory(Duration.ofMinutes(1), true));

        assetIndicators = symbols.stream().map(symbol -> {
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
    }

    private void buildPairs() {
        if (MapUtils.isNotEmpty(assetIndicators)) {
            List<Map.Entry<String, AssetData>> entries = new ArrayList<>(assetIndicators.entrySet());
            this.pairIndicators = new HashSet<>();
            for (int i = 0; i < entries.size(); i++) {
                for (int j = i + 1; j < entries.size(); j++) {
                    var first = entries.get(i);
                    var second = entries.get(j);
                    pairIndicators.add(PairIndicator.of(
                            first.getKey(),
                            second.getKey(),
                            buildPairIndicator(
                                    first.getValue().getFastMaIndicator(),
                                    second.getValue().getFastMaIndicator())));
                }
            }
        }
    }

    private Indicator<Num> buildPairIndicator(Indicator<Num> first, Indicator<Num> second) {
        Indicator<Num> firstLastValueIndicator = LastValueMinusOffsetIndicator.of(first, 1);
        Indicator<Num> secondLastValueIndicator = LastValueMinusOffsetIndicator.of(second, 1);
        return SpreadIndicator.of(firstLastValueIndicator, secondLastValueIndicator);
    }

    private void infillBars(Integer averageDaysCount) {
        Duration slowDuration = DateUtils.toDuration(TimeFrame.TIME_FRAME_D);
        Instant now = Instant.now();
        Instant end = now.minus(slowDuration);
        Instant start = end.minus(slowDuration.multipliedBy(averageDaysCount - 1));

        List<CompletableFuture<Void>> features = assetIndicators.entrySet().stream()
                .map(e -> barService.ta4jConcurrentSeriesAsync(e.getKey(), TimeFrame.TIME_FRAME_D, start, end)
                        .thenAccept(series -> {
                            e.getValue().getSlowMaIndicator().update(series.getBarData());
                        }))
                .toList();
        features.forEach(CompletableFuture::join);

        // Instant now2 = now.minus(Duration.ofDays(2));
        List<CompletableFuture<Void>> fastFeatures = assetIndicators.entrySet().stream()
                .map(e -> barService
                        .ta4jConcurrentSeriesAsync(e.getKey(), TimeFrame.TIME_FRAME_M1,
                                now.minus(Duration.ofMinutes(30)), now)
                        .thenAccept(series -> series.getBarData().forEach(b -> e.getValue().getBarSeries().addBar(b))))
                .toList();
        fastFeatures.forEach(CompletableFuture::join);
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
        AssetData option = assetIndicators.get(symbol);
        ConcurrentBarSeries series = option.getBarSeries();

        if (series.getEndIndex() == -1 || !quote.getTimestamp().isBefore(series.getLastBar().getBeginTime()))
            series.ingestTrade(quote.getTimestamp(), quote.getLastSize(), quote.getLast());
    }

    private void createRebalanceChain(String buySymbol, String sellSymbol, Position sellPosition) {
        Bar targetBar = assetIndicators.get(buySymbol)
                .getBarSeries()
                .getLastBar();

        BigDecimal amount = sellPosition.getCurrentPrice()
                .multiply(sellPosition.getQuantity());

        BigDecimal targetPrice = targetBar.getClosePrice().bigDecimalValue();
        BigDecimal buyQuantity = amount.divide(
                targetPrice,
                0,
                RoundingMode.DOWN);

        if (buyQuantity.signum() == 0) {
            buyQuantity = buyQuantity.add(BigDecimal.ONE);
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
    public class AssetData {
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

    private record PairIndicator(
            String left,
            String right,
            Indicator<Num> indicator) {
        public static PairIndicator of(String aLeft, String aRight, Indicator<Num> aIndicator) {
            return new PairIndicator(aLeft, aRight, aIndicator);
        }

        @Override
        public boolean equals(Object arg0) {
            if (this == arg0) {
                return true;
            }
            if (!(arg0 instanceof PairIndicator other)) {
                return false;
            }
            return (Objects.equals(left, other.left) && Objects.equals(right, other.right))
                    || (Objects.equals(left, other.right) && Objects.equals(right, other.left));
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(left) + Objects.hashCode(right);
        }
    }

    private record PairSpreadValue(
            String first,
            String second,
            Num value) {

        public static PairSpreadValue of(String aFirst, String aSecond, Indicator<Num> indicator) {
            return new PairSpreadValue(aFirst, aSecond, indicator.getValue(0));
        }

        String toSell() {
            return value.isNegative() ? second : value.isPositive() ? first : null;
        }

        String toBuy() {
            return value.isPositive() ? second : value.isNegative() ? first : null;
        }
    }
}

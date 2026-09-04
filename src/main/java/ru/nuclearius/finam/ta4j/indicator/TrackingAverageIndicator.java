package ru.nuclearius.finam.ta4j.indicator;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.util.Assert;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.service.BarService;
import ru.nuclearius.finam.utils.DateUtils;

@Slf4j
@Getter
public class TrackingAverageIndicator implements Indicator<Num> {
    private final String symbol;
    private final BarService barService;
    private final TimeFrame timeFrame;
    private final BarSeries barSeries;
    private final int barsCount;
    private final Duration duration;
    private final Indicator<Num> maIndicator;

    private final AtomicBoolean loading = new AtomicBoolean(false);
    private volatile Instant loadedUntil;

    public static TrackingAverageIndicator of(
            String symbol,
            BarService barService,
            TimeFrame timeFrame,
            int barsCount) {
        return new TrackingAverageIndicator(symbol, barService, timeFrame, barsCount);
    }

    private TrackingAverageIndicator(
            String symbol,
            BarService barService,
            TimeFrame timeFrame,
            int barsCount) {
        Assert.notNull("symbol", "symbol must be present");
        Assert.notNull("barService", "symbol must be present");
        Assert.notNull("timeFrame", "timeFrame must be present");
        Assert.notNull("barsCount", "barsCount must be present");

        this.symbol = symbol;
        this.barService = barService;
        this.timeFrame = timeFrame;
        this.barsCount = barsCount;

        this.duration = DateUtils.toDuration(timeFrame);

        this.barSeries = new BaseBarSeriesBuilder()
                .withName(symbol + "-tracking-series")
                .withMaxBarCount(barsCount)
                .build();

        this.maIndicator = new SMAIndicator(
                new ClosePriceIndicator(barSeries),
                barsCount);
    }

    @Override
    public BarSeries getBarSeries() {
        return barSeries;
    }

    @Override
    public int getCountOfUnstableBars() {
        return barsCount;
    }

    @Override
    public Num getValue(int index) {
        if (loadedUntil == null || loadedUntil.isBefore(Instant.now().minus(duration.multipliedBy(2)))) {
            load();
        }
        if (barSeries.isEmpty() || loading.get())
            return NaN.NaN;

        return maIndicator.getValue(barSeries.getEndIndex());
    }

    private void load() {
        if (!loading.compareAndSet(false, true)) {
            return;
        }
        Instant now = Instant.now();
        Instant start = now.minus(duration.multipliedBy(barsCount + 1));
        Instant end = now.minus(duration);
        log.info("load new period for symbol {}: {} - {}", symbol, start, end);
        barService.barsAsync(symbol, timeFrame, start, end)
                .whenComplete((bars, error) -> {
                    try {
                        if (error != null) {
                            log.error("error loading new period for symbol {}: {}", symbol, error.getMessage());
                            return;
                        }

                        bars.forEach(bar -> barSeries.barBuilder()
                                .timePeriod(duration)
                                .endTime(bar.getTimestamp())
                                .openPrice(bar.getOpen())
                                .highPrice(bar.getHigh())
                                .lowPrice(bar.getLow())
                                .closePrice(bar.getClose())
                                .volume(bar.getVolume())
                                .add());
                        log.info("last bar for symbol {}: {}", symbol, barSeries.getLastBar().getEndTime());
                        loadedUntil = end;
                    } finally {
                        loading.set(false);
                    }
                });
    }
}

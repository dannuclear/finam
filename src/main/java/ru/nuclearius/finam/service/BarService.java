package ru.nuclearius.finam.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.ConcurrentBarSeries;
import org.ta4j.core.ConcurrentBarSeriesBuilder;
import org.ta4j.core.bars.TimeBarBuilderFactory;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.service.domain.BarsWithDescriptiveStatistics;
import ru.nuclearius.finam.utils.DateUtils;
import ru.nuclearius.finam.utils.TimeFrameUtils;

@Service
@RequiredArgsConstructor
public class BarService {
    private final FinamService finamService;

    /**
     * Получение исторических данных по инструменту (агрегированные свечи)
     *
     * @param symbol    Символ инструмента
     * @param timeFrame Необходимый таймфрейм
     * @param startTime Optional. Inclusive start of the interval. If specified, a
     *                  Timestamp matching this interval will have to be the same or
     *                  after the start.
     * @param endTime   Optional. Exclusive end of the interval. If specified, a
     *                  Timestamp matching this interval will have to be before the
     *                  end.
     * @return Агрегированные свечи
     */
    public List<Bar> bars(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {

        return finamService.bars(symbol, timeFrame, startTime, endTime).stream().map(Bar::clone).toList();
    }

    /**
     * Получение исторических данных по инструменту (агрегированные свечи)
     * асинхронно
     *
     * @param symbol    Символ инструмента
     * @param timeFrame Необходимый таймфрейм
     * @param startTime Optional. Inclusive start of the interval. If specified, a
     *                  Timestamp matching this interval will have to be the same or
     *                  after the start.
     * @param endTime   Optional. Exclusive end of the interval. If specified, a
     *                  Timestamp matching this interval will have to be before the
     *                  end.
     * @return Future Агрегированные свечи
     */
    @Async("barsExecutor")
    public CompletableFuture<List<Bar>> barsAsync(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {
        List<Bar> bars = bars(symbol, timeFrame, startTime, endTime);
        return CompletableFuture.completedFuture(bars);
    }

    @Async("barsExecutor")
    public CompletableFuture<BarsWithDescriptiveStatistics> barsWithDescriptiveStatisticsAsync(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {
        List<Bar> bars = bars(symbol, timeFrame, startTime, endTime);
        DescriptiveStatistics ds = new DescriptiveStatistics();
        bars.stream()
                .map(Bar::getClose)
                .map(BigDecimal::doubleValue)
                .forEach(ds::addValue);
        return CompletableFuture.completedFuture(new BarsWithDescriptiveStatistics(bars, ds));
    }

    @Async("barsExecutor")
    public CompletableFuture<BarSeries> ta4jSeriesAsync(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime,
            Boolean concurent) {
        Duration duration = DateUtils.toDuration(timeFrame);
        BarSeries series = concurent
                ? new ConcurrentBarSeriesBuilder().withBarBuilderFactory(new TimeBarBuilderFactory(duration, true))
                        .build()
                : new BaseBarSeriesBuilder().build();

        Instant from = startTime;

        long maxDays = TimeFrameUtils.getDepthDays(timeFrame);

        while (from.isBefore(endTime)) {
            Instant to = from.plus(maxDays, ChronoUnit.DAYS);
            if (to.isAfter(endTime)) {
                to = endTime;
            }
            List<Bar> bars = bars(symbol, timeFrame, from, to);
            for (Bar bar : bars) {
                series.barBuilder()
                        .closePrice(bar.getClose())
                        .openPrice(bar.getOpen())
                        .lowPrice(bar.getLow())
                        .highPrice(bar.getHigh())
                        .volume(bar.getVolume())
                        .timePeriod(duration)
                        .endTime(bar.getTimestamp())
                        .add();
            }
            from = to.plus(duration);
        }

        return CompletableFuture.completedFuture(series);
    }

    @Async("barsExecutor")
    public CompletableFuture<ConcurrentBarSeries> ta4jConcurrentSeriesAsync(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {
        return ta4jSeriesAsync(symbol, timeFrame, startTime, endTime, true)
                .thenApply(series -> (ConcurrentBarSeries) series);
    }
}

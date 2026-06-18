package ru.nuclearius.finam.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.service.domain.BarsWithDescriptiveStatistics;
import ru.nuclearius.finam.utils.DateUtils;

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
    public CompletableFuture<BarSeries> barsSeriesAsync(
            String symbol,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {
        List<Bar> bars = bars(symbol, timeFrame, startTime, endTime);

        BarSeries series = new BaseBarSeriesBuilder().build();
        Duration duration = DateUtils.toDuration(timeFrame);
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
        return CompletableFuture.completedFuture(series);
    }

    // public AssetResult bars(
    //         AssetSetting assetSetting,
    //         TimeFrame timeFrame,
    //         Instant startTime,
    //         Instant endTime,
    //         Integer smaBarCount) {
    //     Assert.notNull(assetSetting, "Инструмент должен быть указан");
    //     Assert.notNull(timeFrame, "Таймфрейм должн быть указан");
    //     Assert.notNull(startTime, "Время начала должно быть указано");
    //     Assert.notNull(endTime, "Время окончания должно быть указано");

    //     List<Bar> bars = finamService.bars(
    //             assetSetting.getSymbol(),
    //             timeFrame,
    //             startTime,
    //             endTime);

    //     BarSeries series = new BaseBarSeriesBuilder().build();
    //     for (Bar bar : bars) {
    //         bar.withPriceOffset(assetSetting.getPriceOffset());
    //         series.barBuilder()
    //                 .closePrice(bar.getClose())
    //                 .timePeriod(DateUtils.toDuration(timeFrame))
    //                 .endTime(bar.getTimestamp())
    //                 .add();
    //     }
    //     ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
    //     SMAIndicator sma = new SMAIndicator(closePrice, smaBarCount);
    //     List<Indicator> smaValues = new ArrayList<>();
    //     for (int i = 0; i < bars.size(); i++) {
    //         Bar bar = bars.get(i);
    //         smaValues.add(Indicator.builder().timestamp(bar.getTimestamp())
    //                 .value(sma.getValue(i).bigDecimalValue()).build());
    //     }
    //     return AssetResult.builder()
    //             .symbol(assetSetting.getSymbol())
    //             .bars(bars)
    //             .sma(smaValues)
    //             .build();
    // }
}

package ru.nuclearius.finam.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import grpc.tradeapi.v1.marketdata.TimeFrame;
import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.rest.dto.Indicator;
import ru.nuclearius.finam.service.domain.AssetResult;
import ru.nuclearius.finam.service.domain.AssetSetting;
import ru.nuclearius.finam.utils.DateUtils;

@Service
@RequiredArgsConstructor
public class BarService {
    private final FinamService finamService;

    public AssetResult bars(
            AssetSetting assetSetting,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime,
            Integer smaBarCount) {
        Assert.notNull(assetSetting, "Инструмент должен быть указан");
        Assert.notNull(timeFrame, "Таймфрейм должн быть указан");
        Assert.notNull(startTime, "Время начала должно быть указано");
        Assert.notNull(endTime, "Время окончания должно быть указано");

        List<Bar> bars = finamService.bars(
                assetSetting.getSymbol(),
                timeFrame,
                startTime,
                endTime);

        BarSeries series = new BaseBarSeriesBuilder().build();
        for (Bar bar : bars) {
            bar.withPriceOffset(assetSetting.getPriceOffset());
            series.barBuilder()
                    .closePrice(bar.getClose())
                    .timePeriod(DateUtils.toDuration(timeFrame))
                    .endTime(bar.getTimestamp())
                    .add();
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, smaBarCount);
        List<Indicator> smaValues = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            smaValues.add(Indicator.builder().timestamp(bar.getTimestamp())
                    .value(sma.getValue(i).bigDecimalValue()).build());
        }
        return AssetResult.builder()
                .symbol(assetSetting.getSymbol())
                .bars(bars)
                .sma(smaValues)
                .build();
    }

    @Async("barsExecutor")
    public CompletableFuture<AssetResult> barsAsync(
            AssetSetting assetSetting,
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime,
            Integer smaBarCount) {

        return CompletableFuture.completedFuture(
                bars(assetSetting, timeFrame, startTime, endTime, smaBarCount))
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    return null;
                });
    }
}

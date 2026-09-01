package ru.nuclearius.finam.strategy;

import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Position;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import ru.nuclearius.finam.rest.dto.ChartIndicatorSeries;
import ru.nuclearius.finam.rest.dto.ChartIndicatorTa4jSeries;
import ru.nuclearius.finam.strategy.rule.AveragingDownRule;

public final class AveragingStrategy implements ChartStrategy {

    private final Strategy strategy;
    private final List<ChartIndicatorSeries> indicators = new ArrayList<>();

    public AveragingStrategy(
            BarSeries series,
            Integer maBarCount,
            double firstEntryPercent,
            double averagingStepPercent,
            int maxAverages,
            double takeProfitPercent) {

        NumFactory numFactory = series.numFactory();
        Indicator<Num> price = new ClosePriceIndicator(series);

        Indicator<Num> maIndicator = new EMAIndicator(price, maBarCount);
        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("moving-average")
                .name("Скользящая средняя")
                .indicator(maIndicator)
                .series(series)
                .lineColor("#0004ff")
                .lineWidth((short) 2)
                .build());

        Indicator<Num> firstEntryBound = NumericIndicator.of(maIndicator).multipliedBy(1 + firstEntryPercent / 100.0);

        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("entry-level")
                .name("Первый вход")
                .indicator(firstEntryBound)
                .series(series)
                .lineColor("#00aa00")
                .build());

        Indicator<Num> firstAveraging = NumericIndicator.of(maIndicator).multipliedBy(1 + (firstEntryPercent + averagingStepPercent) / 100.0);
        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("first-averaging")
                .name("Первое усреднение")
                .indicator(firstAveraging)
                .series(series)
                .lineColor("#027502")
                .build());

        // Rule entry = new UnderIndicatorRule(price, firstEntryBound);

        Rule average = new AveragingDownRule(price, maIndicator, firstEntryPercent, averagingStepPercent, maxAverages);

        Rule exitRule = new CrossedUpIndicatorRule(price, maIndicator);

        strategy = new BaseStrategy(average, exitRule, maBarCount) {
            @Override
            public boolean shouldOperate(int index, TradingRecord tradingRecord) {
                Position position = tradingRecord.getCurrentPosition();
                return shouldEnter(index, tradingRecord)
                        || (shouldExit(index, tradingRecord) && position != null && position.isOpened());
            }
        };
    }

    @Override
    public Strategy getStrategy() {
        return strategy;
    }

    @Override
    public List<ChartIndicatorSeries> indicators() {
        return indicators;
    }
}
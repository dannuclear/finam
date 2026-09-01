package ru.nuclearius.finam.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import ru.nuclearius.finam.rest.dto.ChartIndicatorSeries;
import ru.nuclearius.finam.rest.dto.ChartIndicatorTa4jSeries;

public final class PercentageChartStrategy implements ChartStrategy {
    private Strategy strategy;
    private final List<ChartIndicatorSeries> indicators = new ArrayList<>();

    public PercentageChartStrategy(
            BarSeries series,
            Integer slowMaBarCount,
            Integer fastMaBarCount,
            Double entryPValue,
            Double exitPValue
        ) {
        Objects.requireNonNull(series);

        NumFactory numFactory = series.numFactory();

        Indicator<Num> priceIndicator = new ClosePriceIndicator(series);
        Indicator<Num> maIndicator = new EMAIndicator(priceIndicator, slowMaBarCount);
        Indicator<Num> fastMaIndicator = new SMAIndicator(priceIndicator, fastMaBarCount);

        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("moving-average")
                .name("Скользящая средняя")
                .indicator(maIndicator)
                .series(series)
                .lineColor("#0004ff")
                .lineWidth((short) 2)
                .build());

        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("moving-average-fast")
                .name("Скользящая средняя быстрая")
                .indicator(fastMaIndicator)
                .series(series)
                .lineColor("#f7de00")
                .lineWidth((short) 2)
                .build());

        Indicator<Num> minusPercentLevel = NumericIndicator.of(maIndicator).multipliedBy(1 - (entryPValue / 100.0));
        Indicator<Num> plusPercentLevel = NumericIndicator.of(maIndicator).multipliedBy(1 + (exitPValue / 100.0));

        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("enter-level")
                .name("Уровень входа")
                .indicator(minusPercentLevel)
                .lineColor("#0f4701")
                .series(series)
                .build());

        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("exit-level")
                .name("Уровень выхода")
                .indicator(plusPercentLevel)
                .series(series)
                .lineColor("#ff0000")
                .build());

        Rule entryRule = new UnderIndicatorRule(fastMaIndicator, minusPercentLevel);
        Rule exitRule = new CrossedUpIndicatorRule(fastMaIndicator, plusPercentLevel);
        strategy = new BaseStrategy(entryRule, exitRule);
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

package ru.nuclearius.finam.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.StretchZScoreIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.numeric.BinaryOperationIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import ru.nuclearius.finam.rest.dto.ChartIndicatorSeries;
import ru.nuclearius.finam.rest.dto.ChartIndicatorTa4jSeries;

/**
 * // ChartStrategy chartStrategy = new StretchZScoreChartStrategy(
 * // series,
 * // smaSlow,
 * // smaSlow,
 * // -2.0,
 * // -2 * transactionCost,
 * // 1.0,
 * // 0.0);
 * 
 * // ChartStrategy chartStrategy = new AveragingStrategy(
 * // series,
 * // smaSlow,
 * // -transactionCost * 2,
 * // -0.1, 10,
 * // 0.2);
 */
public final class StretchZScoreChartStrategy implements ChartStrategy {
    private Strategy strategy;
    private final List<ChartIndicatorSeries> indicators = new ArrayList<>();

    public StretchZScoreChartStrategy(
            BarSeries series,
            Integer maBarCount,
            Integer zScoreBarCount,
            Double entryZValue,
            Double entryPValue,
            Double exitZValue,
            Double stopLossZValue) {
        Objects.requireNonNull(series);
        Objects.requireNonNull(entryZValue);
        Objects.requireNonNull(exitZValue);

        NumFactory numFactory = series.numFactory();

        Indicator<Num> priceIndicator = new ClosePriceIndicator(series);
        Indicator<Num> maIndicator = new EMAIndicator(priceIndicator, maBarCount);
        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("moving-average")
                .name("Скользящая средняя")
                .indicator(maIndicator)
                .series(series)
                .lineColor("#0004ff")
                .lineWidth((short) 2)
                .build());

        StretchZScoreIndicator stretchZScoreIndicator = new StretchZScoreIndicator(priceIndicator, maIndicator,
                zScoreBarCount);

        Indicator<Num> entryPriceLevel = NumericIndicator.of(
                stretchZScoreIndicator.getStandardDeviationIndicator())
                .multipliedBy(entryZValue)
                .plus(stretchZScoreIndicator.getReferenceIndicator());
        indicators.add(ChartIndicatorTa4jSeries.builder()
                .id("entry-level")
                .name("Уровень входа")
                .indicator(entryPriceLevel)
                .lineColor("#0f4701")
                .series(series)
                .build());

        Indicator<Num> lowerBound = new ConstantIndicator<>(series, numFactory.numOf(entryZValue));
        Indicator<Num> zeroLine = new ConstantIndicator<>(series, numFactory.numOf(exitZValue));

        Rule entryRule = new UnderIndicatorRule(stretchZScoreIndicator, lowerBound);
        if (entryPValue != null && entryPValue < 0) {
            Indicator<Num> deviationLimit = BinaryOperationIndicator.product(maIndicator, (100 + entryPValue) / 100.0);

            entryRule = entryRule.and(new UnderIndicatorRule(priceIndicator, deviationLimit));
            indicators.add(ChartIndicatorTa4jSeries.builder()
                    .id("entry-percentage-deviation")
                    .name("Порог входа в % от средней")
                    .indicator(deviationLimit)
                    .lineColor("#09ff00")
                    .series(series)
                    .build());
        }

        Rule exitRule = new CrossedUpIndicatorRule(stretchZScoreIndicator, zeroLine);
        if (stopLossZValue != null && stopLossZValue < 0) {
            Indicator<Num> stopLossLine = new ConstantIndicator<>(series, series.numFactory().numOf(stopLossZValue));
            exitRule = exitRule.or(new UnderIndicatorRule(stretchZScoreIndicator, stopLossLine));
        }

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

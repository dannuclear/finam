package ru.nuclearius.finam.strategy.factory.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import ru.nuclearius.finam.rest.dto.ChartIndicatorTa4jSeries;
import ru.nuclearius.finam.strategy.StrategyWithIndicators;
import ru.nuclearius.finam.strategy.factory.ChartStrategyFactory;
import ru.nuclearius.finam.strategy.factory.StrategyParameter;
import ru.nuclearius.finam.strategy.factory.StrategyParameter.Type;

public class MovingAverageStrategyFactory extends ChartStrategyFactory {
    public static final String SLOW_MA_BAR_COUNT_KEY = "slowMa";
    public static final String FAST_MA_BAR_COUNT_KEY = "fastMa";
    public static final String ENTRY_P_VALUE_KEY = "entryP";
    public static final String EXIT_P_VALUE_KEY = "exitP";

    private List<StrategyParameter> parameters = List.of(
            new StrategyParameter(SLOW_MA_BAR_COUNT_KEY, "Количество баров для медленной средней", Type.INTEGER),
            new StrategyParameter(FAST_MA_BAR_COUNT_KEY, "Количество баров для быстрой средней", Type.INTEGER),
            new StrategyParameter(ENTRY_P_VALUE_KEY, "Значение в % для входа", Type.DOUBLE),
            new StrategyParameter(EXIT_P_VALUE_KEY, "Значение в % для выхода", Type.DOUBLE));

    @Override
    public List<StrategyParameter> getParameters() {
        return parameters;
    }

    @Override
    public StrategyWithIndicators build(BarSeries series, Map<String, String> values) {
        Objects.requireNonNull(series);
        Objects.requireNonNull(values);

        int slowMaBarCount = getParameter(values, SLOW_MA_BAR_COUNT_KEY, Integer.class);
        int fastMaBarCount = getParameter(values, FAST_MA_BAR_COUNT_KEY, Integer.class);
        double entryPValue = getParameter(values, ENTRY_P_VALUE_KEY, Double.class);
        double exitPValue = getParameter(values, EXIT_P_VALUE_KEY, Double.class);

        Indicator<Num> priceIndicator = new ClosePriceIndicator(series);
        Indicator<Num> maIndicator = new EMAIndicator(priceIndicator, slowMaBarCount);
        Indicator<Num> fastMaIndicator = new SMAIndicator(priceIndicator, fastMaBarCount);

        Indicator<Num> minusPercentLevel = NumericIndicator.of(maIndicator)
                .multipliedBy(1 - (entryPValue / 100.0));
        Indicator<Num> plusPercentLevel = NumericIndicator.of(maIndicator)
                .multipliedBy(1 + (exitPValue / 100.0));

        Rule entryRule = new UnderIndicatorRule(fastMaIndicator, minusPercentLevel);
        Rule exitRule = new CrossedUpIndicatorRule(fastMaIndicator, plusPercentLevel);

        return new StrategyWithIndicators(new BaseStrategy(entryRule, exitRule),
                List.of(
                        ChartIndicatorTa4jSeries.builder()
                                .id("moving-average")
                                .name("Скользящая средняя")
                                .indicator(maIndicator)
                                .series(series)
                                .lineColor("#0004ff")
                                .lineWidth((short) 2)
                                .build(),
                        ChartIndicatorTa4jSeries.builder()
                                .id("moving-average-fast")
                                .name("Скользящая средняя быстрая")
                                .indicator(fastMaIndicator)
                                .series(series)
                                .lineColor("#f7de00")
                                .lineWidth((short) 2)
                                .build(),
                        ChartIndicatorTa4jSeries.builder()
                                .id("enter-level")
                                .name("Уровень входа")
                                .indicator(minusPercentLevel)
                                .lineColor("#0f4701")
                                .series(series)
                                .build(),
                        ChartIndicatorTa4jSeries.builder()
                                .id("exit-level")
                                .name("Уровень выхода")
                                .indicator(plusPercentLevel)
                                .series(series)
                                .lineColor("#ff0000")
                                .build()));
    }
}
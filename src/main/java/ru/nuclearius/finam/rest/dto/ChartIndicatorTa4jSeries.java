package ru.nuclearius.finam.rest.dto;

import java.util.List;

import org.apache.commons.lang3.IntegerRange;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ChartIndicatorTa4jSeries extends ChartIndicatorSeries {
    private final BarSeries series;
    private final Indicator<Num> indicator;

    @Override
    public List<ChartIndicatorValue> getValues() {
        return IntegerRange.of(series.getBeginIndex(), series.getEndIndex())
                .toIntStream()
                .mapToObj(i -> {
                    Bar bar = series.getBar(i);
                    Num indicatorValue = indicator.getValue(i);
                    return ChartIndicatorValue.builder()
                            .timestamp(bar.getEndTime())
                            .value(indicatorValue != null ? indicatorValue.bigDecimalValue() : null)
                            .build();
                })
                .toList();
    }
}

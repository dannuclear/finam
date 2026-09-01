package ru.nuclearius.finam.utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.rest.dto.ChartBarSeries;
import ru.nuclearius.finam.rest.dto.ChartIndicatorSeries;
import ru.nuclearius.finam.rest.dto.ChartIndicatorValue;

public class BarUtils {
    public static Map<BigDecimal, Pair<Instant, Instant>> groupByValuePeriod(List<Bar> bars) {
        Map<BigDecimal, Pair<Instant, Instant>> result = new LinkedHashMap<>();

        if (bars == null || bars.size() == 0)
            return result;

        Bar first = bars.get(0);
        BigDecimal currnetRate = first.getClose();
        Instant startInstance = first.getTimestamp();

        for (int i = 1; i < bars.size(); i++) {
            Bar current = bars.get(i);
            if (current.getClose().compareTo(currnetRate) != 0) {
                result.put(currnetRate, Pair.of(startInstance, current.getTimestamp()));
                currnetRate = current.getClose();
                startInstance = current.getTimestamp();
            }
        }
        result.put(currnetRate, Pair.of(startInstance, null));
        return result;
    }

    public static ChartBarSeries buildSeries(BarSeries series, String id, String name) {
        List<Bar> bars = new ArrayList<>(series.getBarCount());
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            org.ta4j.core.Bar sBar = series.getBar(i);
            Bar bar = new Bar();
            bar.setClose(sBar.getClosePrice().bigDecimalValue());
            bar.setHigh(sBar.getHighPrice().bigDecimalValue());
            bar.setLow(sBar.getLowPrice().bigDecimalValue());
            bar.setOpen(sBar.getOpenPrice().bigDecimalValue());
            bar.setTimestamp(sBar.getEndTime());
            bar.setVolume(sBar.getVolume().bigDecimalValue());
            bars.add(bar);
        }
        return ChartBarSeries.builder().values(bars).build();
    }

    public static ChartIndicatorSeries toChartSeries(
            BarSeries barSeries,
            String id,
            String name,
            Indicator<Num> indicator) {

        List<ChartIndicatorValue> values = new ArrayList<>(barSeries.getBarCount());

        for (int i = barSeries.getBeginIndex(); i <= barSeries.getEndIndex(); i++) {
            org.ta4j.core.Bar bar = barSeries.getBar(i);

            values.add(
                    ChartIndicatorValue.builder()
                            .timestamp(bar.getEndTime())
                            .value(numToBigDecimal(indicator.getValue(i)))
                            .build());
        }

        return ChartIndicatorSeries.builder()
                .id(id)
                .name(name)
                .values(values)
                .build();
    }

    public static BigDecimal numToBigDecimal(Num num) {
        if (num == null)
            return null;
        return num.bigDecimalValue();
    }
}

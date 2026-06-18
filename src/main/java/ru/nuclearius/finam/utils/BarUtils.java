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

import ru.nuclearius.finam.client.dto.Bar;
import ru.nuclearius.finam.rest.dto.Series;

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

    public static List<Series> buildSeries(BarSeries series, List<Indicator<?>> indicators, String id, String name) {
        List<Bar> bars = new ArrayList<>(series.getBarCount());
        for (int i = series.getBeginIndex(); i < series.getBarCount(); i++) {
            org.ta4j.core.Bar sBar = series.getBar(i);
            Bar bar = new Bar();
            bar.setClose(sBar.getClosePrice().bigDecimalValue());
            bar.setHigh(sBar.getHighPrice().bigDecimalValue());
            bar.setLow(sBar.getLowPrice().bigDecimalValue());
            bar.setOpen(sBar.getOpenPrice().bigDecimalValue());
            bar.setTimestamp(sBar.getEndTime());
            bar.setVolume(sBar.getVolume().bigDecimalValue());
        }
        return null;
    }
}

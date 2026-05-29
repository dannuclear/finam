package ru.nuclearius.finam.utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import ru.nuclearius.finam.client.dto.Bar;

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
}

package ru.nuclearius.finam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.ta4j.core.ConcurrentBarSeries;
import org.ta4j.core.ConcurrentBarSeriesBuilder;
import org.ta4j.core.bars.TimeBarBuilderFactory;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.NaN;

import ru.nuclearius.finam.streamer.LastValueMinusOffsetIndicator;

public class IndicatorTest {

    @Test
    void testLastValueIndicator() {
        ConcurrentBarSeries barSeries = new ConcurrentBarSeriesBuilder()
                .withBarBuilderFactory(new TimeBarBuilderFactory(Duration.ofMinutes(1), true))
                .build();
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(barSeries);
        LastValueMinusOffsetIndicator indicator = LastValueMinusOffsetIndicator.of(closePriceIndicator, 1);

        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        barSeries.ingestTrade(now, 1, 50.0);
        assertEquals(indicator.getValue(0), NaN.NaN);
        barSeries.ingestTrade(now.plusSeconds(20), 1, 50.0);
        assertEquals(indicator.getValue(0), NaN.NaN);
        barSeries.ingestTrade(now.plusSeconds(60), 1, 60.0);
        assertEquals(indicator.getValue(0).doubleValue(), 50.0);
        barSeries.ingestTrade(now.plusSeconds(2 * 60), 1, 60.0);
        assertEquals(indicator.getValue(0).doubleValue(), 60.0);
    }
}

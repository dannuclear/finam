package ru.nuclearius.finam.ta4j.indicator;

import java.util.Collection;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import lombok.Getter;

@Getter
public class LastAverageIndicator implements Indicator<Num> {

    private final int barsCount;
    private final BarSeries barSeries;
    private final Indicator<Num> averageIndicator;

    public static LastAverageIndicator of(BarSeries barSeries, int barsCount) {
        return new LastAverageIndicator(barSeries, barsCount);
    }

    private LastAverageIndicator(BarSeries barSeries, int barsCount) {
        this.barsCount = barsCount;

        barSeries.setMaximumBarCount(barsCount);
        this.barSeries = barSeries;

        this.averageIndicator = new SMAIndicator(
                new ClosePriceIndicator(barSeries),
                barsCount);
    }

    @Override
    public BarSeries getBarSeries() {
        return barSeries;
    }

    @Override
    public int getCountOfUnstableBars() {
        return barsCount;
    }

    @Override
    public Num getValue(int index) {
        if (barSeries.isEmpty()) {
            return NaN.NaN;
        }

        return averageIndicator.getValue(barSeries.getEndIndex());
    }

    public void update(Collection<Bar> bars) {
        bars.forEach(barSeries::addBar);
    }
}
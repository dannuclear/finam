package ru.nuclearius.finam.streamer;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.num.Num;

public class LastValueMinusOffsetIndicator implements Indicator<Num> {

    private final Indicator<Num> source;

    private LastValueMinusOffsetIndicator(Indicator<Num> source, int n) {
        this.source = new PreviousValueIndicator(source, n);
    }

    public static LastValueMinusOffsetIndicator of(Indicator<Num> source, int n) {
        return new LastValueMinusOffsetIndicator(source, n);
    }

    @Override
    public Num getValue(int index) {
        BarSeries series = source.getBarSeries();
        return source.getValue(series.getEndIndex());
    }

    @Override
    public int getCountOfUnstableBars() {
        return source.getCountOfUnstableBars();
    }

    @Override
    public BarSeries getBarSeries() {
        return source.getBarSeries();
    }
}
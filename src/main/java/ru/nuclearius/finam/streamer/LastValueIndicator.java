package ru.nuclearius.finam.streamer;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

public class LastValueIndicator implements Indicator<Num> {

    private final Indicator<Num> source;
    private final int offset;

    public LastValueIndicator(Indicator<Num> source, int offset) {
        this.source = source;
        this.offset = offset;
    }

    @Override
    public Num getValue(int index) {
        BarSeries series = source.getBarSeries();
        return source.getValue(series.getEndIndex() - offset);
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
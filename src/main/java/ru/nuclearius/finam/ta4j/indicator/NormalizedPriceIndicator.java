package ru.nuclearius.finam.ta4j.indicator;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

import lombok.Getter;

@Getter
public class NormalizedPriceIndicator extends CachedIndicator<Num> {
    private final Indicator<Num> priceIndicator;
    private final Indicator<Num> slowMaIndicator;
    private final NumFactory factory = DecimalNumFactory.getInstance();

    public NormalizedPriceIndicator(
            BarSeries priceSeries,
            Indicator<Num> slowMaIndicator) {
        super(priceSeries);
        this.priceIndicator = new ClosePriceIndicator(priceSeries);
        this.slowMaIndicator = slowMaIndicator;
    }

    @Override
    public int getCountOfUnstableBars() {
        return priceIndicator.getCountOfUnstableBars();
    }

    @Override
    protected Num calculate(int index) {
        BarSeries slowBarSeries = slowMaIndicator.getBarSeries();
        Num mean = slowMaIndicator.getValue(slowBarSeries.getEndIndex());

        Num val = priceIndicator.getValue(index);
        return val.dividedBy(mean)
                .minus(factory.one())
                .multipliedBy(factory.hundred());
    }
}

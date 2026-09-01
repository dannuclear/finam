package ru.nuclearius.finam.strategy.rule;

import org.ta4j.core.Indicator;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.AbstractRule;

public class AveragingDownRule extends AbstractRule {

    private final Indicator<Num> price;
    private final Indicator<Num> ma;
    private final double firstEntryPercent;
    private final double stepPercent;
    private final int maxAdds;

    private int count = 0;
    private Num lastBuyPrice;

    public AveragingDownRule(
            Indicator<Num> price,
            Indicator<Num> ma,
            double firstEntryPercent,
            double stepPercent,
            int maxAdds) {
        this.price = price;
        this.ma = ma;
        this.firstEntryPercent = firstEntryPercent;
        this.stepPercent = stepPercent;
        this.maxAdds = maxAdds;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {

        Num current = price.getValue(index);
        NumFactory numFactory = price.getBarSeries().numFactory();
        Position position = tradingRecord.getCurrentPosition();

        if ((position == null || position.isNew()) && count == 0) {
            Num firstEntryLevel = ma.getValue(index).multipliedBy(numFactory.numOf(1 + firstEntryPercent / 100));
            if (price.getValue(index).isLessThan(firstEntryLevel)) {
                count++;
                return true;
            }
        }

        if (position.isClosed() || count >= maxAdds || count == 0) {
            return false;
        }

        Num level = ma.getValue(index).multipliedBy(numFactory.numOf(1 + (firstEntryPercent + (stepPercent * (count))) / 100));

        if (price.getValue(index).isLessThan(level)) {
            count++;
            return true;
        }

        return false;
    }
}
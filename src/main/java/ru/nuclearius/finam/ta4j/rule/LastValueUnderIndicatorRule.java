package ru.nuclearius.finam.ta4j.rule;

import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import ru.nuclearius.finam.streamer.LastValueMinusOffsetIndicator;

public class LastValueUnderIndicatorRule extends AbstractRule {
    private final Indicator<Num> up;
    private final Indicator<Num> down;
    private final Rule rule;

    public static LastValueUnderIndicatorRule of(Indicator<Num> first, Indicator<Num> second, Integer secondOffset) {
        return new LastValueUnderIndicatorRule(first, second, secondOffset);
    }

    private LastValueUnderIndicatorRule(Indicator<Num> first, Indicator<Num> second, Integer secondOffset) {
        this.up = first;
        this.down = LastValueMinusOffsetIndicator.of(second, secondOffset);
        this.rule = new UnderIndicatorRule(first, this.down);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        if (index < Math.max(up.getCountOfUnstableBars(), down.getCountOfUnstableBars()))
            return false;
        return rule.isSatisfied(index, tradingRecord);
    }
}

package ru.nuclearius.finam.ta4j.indicator;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

public class SpreadIndicator implements Indicator<Num> {
    private final Indicator<Num> left;
    private final Indicator<Num> right;

    public static SpreadIndicator of(Indicator<Num> left, Indicator<Num> right) {
        return new SpreadIndicator(left, right);
    }

    public SpreadIndicator(Indicator<Num> left, Indicator<Num> right) {
        if (left != null && right != null) {
            this.left = left;
            this.right = right;
        } else {
            throw new IllegalArgumentException("Indicators must not be null");
        }
    }

    @Override
    public BarSeries getBarSeries() {
        return this.left.getBarSeries();
    }

    @Override
    public int getCountOfUnstableBars() {
        return Math.max(this.left.getCountOfUnstableBars(), this.right.getCountOfUnstableBars());
    }

    @Override
    public Num getValue(int index) {
        Num n1 = this.left.getValue(index);
        Num n2 = this.right.getValue(index);
        return n1.minus(n2);
    }
}

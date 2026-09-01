package ru.nuclearius.finam.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.data.geo.Point;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

public class StatisticsUtils {

    public static List<Point> getRealMarketDistribution(BarSeries series, Integer maBarCount, int binCount) {
        Indicator<Num> priceIndicator = new ClosePriceIndicator(series);
        Indicator<Num> maIndicator = new SMAIndicator(priceIndicator, maBarCount);

        Indicator<Num> deviationPercent = NumericIndicator.of(priceIndicator).dividedBy(maIndicator).minus(1)
                .multipliedBy(100);

        DescriptiveStatistics deviationStats = new DescriptiveStatistics();

        // Собираем реальные отклонения цены от EMA в %
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            double deviation = deviationPercent.getValue(i).doubleValue();

            if (Double.isFinite(deviation)) {
                deviationStats.addValue(deviation);
            }
        }

        if (deviationStats.getN() == 0) {
            return List.of();
        }

        double min = deviationStats.getMin();
        double max = deviationStats.getMax();

        if (min == max) {
            return List.of(new Point(min, 1.0));
        }

        double binWidth = (max - min) / binCount;
        double[] bins = new double[binCount];

        // Распределяем значения по bins
        for (double deviation : deviationStats.getValues()) {
            int binIndex = (int) ((deviation - min) / binWidth);
            if (binIndex >= binCount) {
                binIndex = binCount - 1;
            }
            if (binIndex >= 0) {
                bins[binIndex]++;
            }
        }

        // Нормализуем в probability density
        double totalElements = deviationStats.getN();
        List<Point> result = new ArrayList<>(binCount);
        for (int i = 0; i < binCount; i++) {
            double binCenter = min + i * binWidth + binWidth / 2.0;
            double density = bins[i] / (totalElements * binWidth);
            result.add(new Point(binCenter, density));
        }
        return result;
    }
}
